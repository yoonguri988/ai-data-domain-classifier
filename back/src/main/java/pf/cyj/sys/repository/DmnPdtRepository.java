package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.DmnPdt;

/** DOMAIN_PREDICTION - AI 판별 결과 Top-N 조회 (predictionRank 오름차순 = 1순위부터) */
/** AI 판별 결과(DOMAIN_PREDICTION) - 컬럼별 추천순위(Top-N) 오름차순 조회 */
public interface DmnPdtRepository extends JpaRepository<DmnPdt, Long> {

    List<DmnPdt> findByAnlCol_ColumnIdOrderByPredictionRankAsc(Long columnId);
}
