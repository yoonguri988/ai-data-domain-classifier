package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.type.DsetStatCd;

/** 분석대상 데이터셋(ANALYSIS_DATASET, 업무키 PK) - 상태/등록자별 조회, 업무번호 중복 체크 */
public interface AnlDsetRepository extends JpaRepository<AnlDset, String> {

    boolean existsByRequestNo(String requestNo);

    List<AnlDset> findByDatasetStatus(DsetStatCd datasetStatus);

    List<AnlDset> findByRequestedBy_UserId(Long userId);
}
