package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AnlJob;
import pf.cyj.sys.entity.type.JobStatCd;

public interface AnlJobRepository extends JpaRepository<AnlJob, Long> {

    List<AnlJob> findByJobStatus(JobStatCd jobStatus);

    List<AnlJob> findByAnlDset_DatasetId(String datasetId);
}
