package pf.cyj.sys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.CmnCdGrp;

/** 공통코드 그룹(COMMON_CODE_GROUP) - 코드그룹 단건/전체 조회 */
public interface CmnCdGrpRepository extends JpaRepository<CmnCdGrp, String> {
}
