package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.CmnCd;
import pf.cyj.sys.entity.CmnCdId;

/** 공통코드 상세(COMMON_CODE, 복합키) - 그룹별 정렬순서 조회, 사용중(useYn) 필터 조회 */
public interface CmnCdRepository extends JpaRepository<CmnCd, CmnCdId> {

    List<CmnCd> findByCodeGroupOrderBySortOrderAsc(String codeGroup);

    List<CmnCd> findByCodeGroupAndUseYnTrueOrderBySortOrderAsc(String codeGroup);
}
