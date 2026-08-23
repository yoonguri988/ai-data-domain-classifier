package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.RptExpLog;

public interface RptExpLogRepository extends JpaRepository<RptExpLog, Long> {

    List<RptExpLog> findByAnlDset_DatasetIdOrderByExportedAtDesc(String datasetId);
}
