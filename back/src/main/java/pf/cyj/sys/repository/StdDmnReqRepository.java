package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.StdDmnReq;
import pf.cyj.sys.entity.type.ReqStatCd;

/** STANDARD_DOMAIN_REQUEST - 담당자 승인 대기열 조회 */
public interface StdDmnReqRepository extends JpaRepository<StdDmnReq, Long> {

    List<StdDmnReq> findByRequestStatusOrderByRequestedAtAsc(ReqStatCd requestStatus);

    List<StdDmnReq> findByRequestedBy_UserId(Long userId);
}
