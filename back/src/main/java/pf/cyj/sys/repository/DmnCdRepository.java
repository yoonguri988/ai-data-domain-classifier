package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.DmnCd;

/** 표준 도메인 후보 마스터(DOMAIN_CODE) - 정렬순서 오름차순 전체 조회 */
public interface DmnCdRepository extends JpaRepository<DmnCd, String> {

    List<DmnCd> findAllByOrderBySortOrderAsc();
}
