package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.DmnPdt;

/** AI 판별 결과(DOMAIN_PREDICTION) - 컬럼별 추천순위(Top-N) 오름차순 조회 */
public interface DmnPdtRepository extends JpaRepository<DmnPdt, Long> {

    List<DmnPdt> findByAnlCol_ColumnIdOrderByPredictionRankAsc(Long columnId);

    /** UQ_DOMAIN_PREDICTION(COLUMN_ID, DOMAIN_CODE) 위반 여부를 INSERT 전에 미리 확인하기 위한 조회. */
    boolean existsByAnlCol_ColumnIdAndDmnCd_DomainCode(Long columnId, String domainCode);

    /**
     * 재판별 시 기존 Top-N을 지우고 새로 저장하기 위한 삭제.
     * UQ_DOMAIN_PREDICTION(COLUMN_ID, DOMAIN_CODE) 유니크 제약 때문에, 같은 컬럼을 다시 판별할 때
     * 이전 결과를 먼저 지우지 않고 새 결과를 INSERT 하면 ORA-00001로 실패한다 - DmnPdtService.predict() 참고.
     */
    void deleteByAnlCol_ColumnId(Long columnId);
}
