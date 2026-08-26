package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AnlJob;
import pf.cyj.sys.entity.type.JobStatCd;

/** 배치작업(ANALYSIS_JOB) - 실행 상태별, 데이터셋별 작업 목록 조회 */
public interface AnlJobRepository extends JpaRepository<AnlJob, Long> {

    List<AnlJob> findByJobStatus(JobStatCd jobStatus);

    List<AnlJob> findByAnlDset_DatasetId(String datasetId);
}
