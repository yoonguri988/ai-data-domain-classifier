package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.DmnPdt;

/** DOMAIN_PREDICTION - AI 판별 결과 Top-N 조회 (predictionRank 오름차순 = 1순위부터) */
public interface DmnPdtRepository extends JpaRepository<DmnPdt, Long> {

    List<DmnPdt> findByAnlCol_ColumnIdOrderByPredictionRankAsc(Long columnId);
}
