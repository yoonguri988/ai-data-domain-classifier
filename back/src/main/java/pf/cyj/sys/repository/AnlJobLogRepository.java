package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AnlJobLog;

public interface AnlJobLogRepository extends JpaRepository<AnlJobLog, Long> {

    List<AnlJobLog> findByAnlJob_JobIdOrderByLogIdDesc(Long jobId);
}
