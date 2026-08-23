package pf.cyj.sys.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.StdDmn;

/** STANDARD_DOMAIN - 확정본. PK(COLUMN_ID)가 ANALYSIS_COLUMN 과 공유(@MapsId)되므로 컬럼 ID로 바로 조회 가능 */
public interface StdDmnRepository extends JpaRepository<StdDmn, Long> {

    Optional<StdDmn> findByAnlCol_ColumnId(Long columnId);
}
