package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.CmnCd;
import pf.cyj.sys.entity.CmnCdId;

public interface CmnCdRepository extends JpaRepository<CmnCd, CmnCdId> {

    List<CmnCd> findByCodeGroupOrderBySortOrderAsc(String codeGroup);

    List<CmnCd> findByCodeGroupAndUseYnTrueOrderBySortOrderAsc(String codeGroup);
}
