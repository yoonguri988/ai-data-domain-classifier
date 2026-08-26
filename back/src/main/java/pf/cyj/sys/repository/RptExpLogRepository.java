package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.RptExpLog;

/** 리포트 출력 이력(REPORT_EXPORT_LOG) - 데이터셋별 최신순 조회 */
public interface RptExpLogRepository extends JpaRepository<RptExpLog, Long> {

    List<RptExpLog> findByAnlDset_DatasetIdOrderByExportedAtDesc(String datasetId);
}
