package pf.cyj.sys.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.AnlJob;
import pf.cyj.sys.entity.AnlJobLog;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.type.ExecStatCd;
import pf.cyj.sys.entity.type.JobStatCd;
import pf.cyj.sys.entity.type.SchdTypCd;

/** 배치작업 Repository 테스트 - AnlJob, AnlJobLog(실행 이력). */
@SpringBootTest
@Transactional
class AnlJobRepositoryTest {

    @Autowired
    AppUsrRepository appUsrRepository;
    @Autowired
    AnlDsetRepository anlDsetRepository;
    @Autowired
    AnlJobRepository anlJobRepository;
    @Autowired
    AnlJobLogRepository anlJobLogRepository;

    private AppUsr operator;
    private AnlDset savedDset;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();
        String sfx = String.valueOf(System.nanoTime() % 100_000);

        operator = appUsrRepository.save(
                AppUsr.builder()
                        .loginId("job-usr-" + ts)
                        .userName("배치운영자")
                        .email("job-usr-" + ts + "@example.com")
                        .build()
        );

        savedDset = anlDsetRepository.save(
                AnlDset.builder()
                        .datasetId("DS_" + sfx)
                        .requestNo("RQ_" + sfx)
                        .datasetName("배치테스트 데이터셋")
                        .dbSchemaName("TEST_SCHEMA")
                        .tableName("TEST_TABLE")
                        .requestedBy(operator)
                        .build()
        );
    }

    @Test
    @DisplayName("ANALYSIS_JOB 등록 및 상태/데이터셋별 조회")
    void testAnlJobFindByStatusAndDataset() {
        AnlJob job = anlJobRepository.save(
                AnlJob.builder()
                        .jobName("야간 재판별 배치")
                        .anlDset(savedDset)
                        .createdBy(operator)
                        .build()
        );

        assertThat(job.getJobStatus()).isEqualTo(JobStatCd.READY);
        assertThat(job.getScheduleType()).isEqualTo(SchdTypCd.ONCE);

        List<AnlJob> ready = anlJobRepository.findByJobStatus(JobStatCd.READY);
        assertThat(ready).extracting(AnlJob::getJobId).contains(job.getJobId());

        List<AnlJob> byDataset = anlJobRepository.findByAnlDset_DatasetId(savedDset.getDatasetId());
        assertThat(byDataset).extracting(AnlJob::getJobId).contains(job.getJobId());
    }

    @Test
    @DisplayName("ANALYSIS_JOB_LOG 실행 이력 등록 및 작업별 최신순 조회")
    void testAnlJobLogFindByJobOrderedDesc() {
        AnlJob job = anlJobRepository.save(
                AnlJob.builder().jobName("컬럼 스캔 배치").anlDset(savedDset).createdBy(operator).build()
        );

        AnlJobLog log1 = anlJobLogRepository.save(
                AnlJobLog.builder()
                        .anlJob(job)
                        .execStatus(ExecStatCd.SUCCESS)
                        .successCount(10)
                        .failCount(0)
                        .executedBy(operator)
                        .build()
        );
        AnlJobLog log2 = anlJobLogRepository.save(
                AnlJobLog.builder()
                        .anlJob(job)
                        .execStatus(ExecStatCd.CALL_ERROR)
                        .errorMessage("외부 AI API 타임아웃")
                        .executedBy(operator)
                        .build()
        );

        List<AnlJobLog> logs = anlJobLogRepository.findByAnlJob_JobIdOrderByLogIdDesc(job.getJobId());

        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getLogId()).isEqualTo(log2.getLogId());
        assertThat(logs.get(1).getLogId()).isEqualTo(log1.getLogId());
    }
}
