package pf.cyj.sys.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.dto.request.AnlJobCreateReq;
import pf.cyj.sys.dto.response.AnlJobLogRsp;
import pf.cyj.sys.dto.response.AnlJobRsp;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.AnlJob;
import pf.cyj.sys.entity.AnlJobLog;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.type.ExecStatCd;
import pf.cyj.sys.entity.type.JobStatCd;
import pf.cyj.sys.entity.type.SchdTypCd;
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.repository.AnlDsetRepository;
import pf.cyj.sys.repository.AnlJobLogRepository;
import pf.cyj.sys.repository.AnlJobRepository;
import pf.cyj.sys.repository.AppUsrRepository;

/**
 * 배치작업 - 분석/재판별 배치 등록 및 실행 이력 관리.
 * startExecution/completeExecution/failExecution 은 이후 구현할 템플릿 메서드 기반 배치 실행기
 * (AbstractAnlJobExecutor)가 호출하는 내부용 API 로, 외부에 공개 REST 요청으로는 노출하지 않는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnlJobService {

    private final AppUsrRepository appUsrRepository;
    private final AnlDsetRepository anlDsetRepository;
    private final AnlJobRepository anlJobRepository;
    private final AnlJobLogRepository anlJobLogRepository;

    @Transactional
    public AnlJobRsp createJob(AnlJobCreateReq req, Long creatorId) {
        AppUsr creator = appUsrRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다: " + creatorId));

        AnlDset dset = null;
        if (req.datasetId() != null && !req.datasetId().isBlank()) {
            dset = anlDsetRepository.findById(req.datasetId())
                    .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 데이터셋입니다: " + req.datasetId()));
        }

        AnlJob job = AnlJob.builder()
                .jobName(req.jobName())
                .anlDset(dset)
                .createdBy(creator)
                .cronExpr(req.cronExpr())
                .build();

        if (req.jobType() != null && !req.jobType().isBlank()) {
            job.setJobType(req.jobType());
        }
        if (req.scheduleType() != null && !req.scheduleType().isBlank()) {
            job.setScheduleType(SchdTypCd.valueOf(req.scheduleType()));
        }

        return AnlJobRsp.from(anlJobRepository.save(job));
    }

    public List<AnlJobRsp> findJobsByStatus(JobStatCd status) {
        return anlJobRepository.findByJobStatus(status).stream().map(AnlJobRsp::from).toList();
    }

    public List<AnlJobRsp> findJobsByDataset(String datasetId) {
        return anlJobRepository.findByAnlDset_DatasetId(datasetId).stream().map(AnlJobRsp::from).toList();
    }

    public List<AnlJobLogRsp> findLogsByJob(Long jobId) {
        return anlJobLogRepository.findByAnlJob_JobIdOrderByLogIdDesc(jobId).stream().map(AnlJobLogRsp::from).toList();
    }

    @Transactional
    public AnlJobLogRsp startExecution(Long jobId, Long executorId) {
        AnlJob job = anlJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 작업입니다: " + jobId));

        job.setJobStatus(JobStatCd.RUNNING);

        AppUsr executor = executorId != null ? appUsrRepository.findById(executorId).orElse(null) : null;

        AnlJobLog log = anlJobLogRepository.save(
                AnlJobLog.builder()
                        .anlJob(job)
                        .execStatus(ExecStatCd.RUNNING)
                        .startedAt(LocalDateTime.now())
                        .executedBy(executor)
                        .build()
        );

        return AnlJobLogRsp.from(log);
    }

    @Transactional
    public AnlJobLogRsp completeExecution(Long logId, int successCount, int failCount) {
        AnlJobLog log = anlJobLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 실행 이력입니다: " + logId));

        log.setExecStatus(ExecStatCd.SUCCESS);
        log.setEndedAt(LocalDateTime.now());
        log.setSuccessCount(successCount);
        log.setFailCount(failCount);
        log.getAnlJob().setJobStatus(JobStatCd.SUCCESS);

        return AnlJobLogRsp.from(log);
    }

    @Transactional
    public AnlJobLogRsp failExecution(Long logId, ExecStatCd errorType, String errorMessage) {
        if (errorType != ExecStatCd.INTERNAL_ERROR && errorType != ExecStatCd.CALL_ERROR) {
            throw new IllegalArgumentException("실패 상태는 INTERNAL_ERROR 또는 CALL_ERROR 만 허용됩니다: " + errorType);
        }

        AnlJobLog log = anlJobLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 실행 이력입니다: " + logId));

        log.setExecStatus(errorType);
        log.setEndedAt(LocalDateTime.now());
        log.setErrorMessage(errorMessage);
        log.getAnlJob().setJobStatus(JobStatCd.FAILED);

        return AnlJobLogRsp.from(log);
    }
}
