package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AnlJobLog;

/** 배치작업 실행 이력(ANALYSIS_JOB_LOG) - 작업별 최신 실행 이력 순 조회 */
public interface AnlJobLogRepository extends JpaRepository<AnlJobLog, Long> {

    List<AnlJobLog> findByAnlJob_JobIdOrderByLogIdDesc(Long jobId);
}
