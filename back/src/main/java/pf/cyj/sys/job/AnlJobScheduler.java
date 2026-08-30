package pf.cyj.sys.job;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import pf.cyj.sys.entity.AnlJob;
import pf.cyj.sys.entity.type.JobStatCd;
import pf.cyj.sys.entity.type.SchdTypCd;
import pf.cyj.sys.repository.AnlJobLogRepository;
import pf.cyj.sys.repository.AnlJobRepository;

/**
 * READY 상태 배치작업을 찾아 자동으로 실행하는 내장 스케줄러 - 원본 솔루션의 Quartz + 쉘 스크립트
 * 기동 방식을 대체한다(설계서 3-4절). 별도 프로세스 기동/쉘 실행이 없어서 컨테이너 환경에서도 그대로
 * 동작한다는 게 핵심 개선점이다.
 *
 * <p>scheduleType 에 따라 처리가 다르다:
 * <ul>
 *   <li>ONCE - READY 가 되는 즉시 1회 실행하고 끝난다(SUCCESS/FAILED 로 남아 다시 자동 실행되지 않는다).</li>
 *   <li>CRON - cronExpr 기준으로 실행 시각이 됐을 때만 실행하고, 실행이 끝나면 다시 READY 로 되돌려서
 *       다음 주기에도 이 스케줄러가 계속 찾아 실행하게 한다(그렇지 않으면 SUCCESS 로 멈춰서 한 번만 돌고 만다).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class AnlJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnlJobScheduler.class);

    /** 이 스케줄러가 처리하는 jobType. 다른 jobType 이 추가되면 이 목록/분기도 함께 늘어난다. */
    private static final String JOB_TYPE_DOMAIN_PREDICT = "DOMAIN_PREDICT";

    private final AnlJobRepository anlJobRepository;
    private final AnlJobLogRepository anlJobLogRepository;
    private final DomainPredictJobExecutor domainPredictJobExecutor;

    /** 1분마다 READY 상태 배치작업을 훑어 실행 시각이 된 것만 실행한다. */
    @Scheduled(fixedRate = 60_000)
    public void runReadyJobs() {
        List<AnlJob> readyJobs = anlJobRepository.findByJobStatus(JobStatCd.READY);
        for (AnlJob job : readyJobs) {
            if (!JOB_TYPE_DOMAIN_PREDICT.equals(job.getJobType())) {
                continue; // 이 스케줄러는 도메인 재판별 작업만 처리한다.
            }
            if (job.getScheduleType() == SchdTypCd.CRON && !isDue(job)) {
                continue;
            }

            log.info("[AnlJobScheduler] 배치작업 실행 시작. jobId={}, jobName={}", job.getJobId(), job.getJobName());
            domainPredictJobExecutor.execute(job.getJobId(), null);

            if (job.getScheduleType() == SchdTypCd.CRON) {
                // 실행 중 상태가 SUCCESS/FAILED 로 바뀌었으므로, 다음 주기에도 이 스케줄러가 다시 찾을 수 있도록
                // 최신 상태를 다시 조회해 READY 로 되돌린다.
                anlJobRepository.findById(job.getJobId()).ifPresent(refreshed -> {
                    refreshed.setJobStatus(JobStatCd.READY);
                    anlJobRepository.save(refreshed);
                });
            }
        }
    }

    /** CRON 작업의 실행 시각이 됐는지 판단한다 - 마지막 실행 시작 시각(없으면 등록 시각) 기준으로 다음 실행 시각을 계산한다. */
    private boolean isDue(AnlJob job) {
        if (job.getCronExpr() == null || job.getCronExpr().isBlank()) {
            log.warn("[AnlJobScheduler] scheduleType=CRON 인데 cronExpr 이 비어있어 건너뜁니다. jobId={}", job.getJobId());
            return false;
        }

        LocalDateTime lastRun = anlJobLogRepository.findByAnlJob_JobIdOrderByLogIdDesc(job.getJobId())
                .stream()
                .findFirst()
                .map(jobLog -> jobLog.getStartedAt())
                .orElse(job.getCreatedAt());

        try {
            CronExpression cron = CronExpression.parse(job.getCronExpr());
            LocalDateTime nextFireTime = cron.next(lastRun);
            return nextFireTime != null && !nextFireTime.isAfter(LocalDateTime.now());
        } catch (IllegalArgumentException e) {
            log.warn("[AnlJobScheduler] cronExpr 형식이 올바르지 않아 건너뜁니다. jobId={}, cronExpr={}", job.getJobId(), job.getCronExpr());
            return false;
        }
    }
}
