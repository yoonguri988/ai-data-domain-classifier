package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AnlCol;

/** 분석대상 컬럼 메타(ANALYSIS_COLUMN) - 데이터셋별 컬럼 목록 조회 */
public interface AnlColRepository extends JpaRepository<AnlCol, Long> {

    List<AnlCol> findByAnlDset_DatasetId(String datasetId);
}
