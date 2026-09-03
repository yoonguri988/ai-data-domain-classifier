package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.StdDmnReq;
import pf.cyj.sys.entity.type.ReqStatCd;

/** STANDARD_DOMAIN_REQUEST - 담당자 승인 대기열 조회 */
/** 표준 도메인 확정 신청본(STANDARD_DOMAIN_REQUEST) - 대기중 신청 목록, 신청자별 이력 조회 */
public interface StdDmnReqRepository extends JpaRepository<StdDmnReq, Long> {

    List<StdDmnReq> findByRequestStatusOrderByRequestedAtAsc(ReqStatCd requestStatus);

    List<StdDmnReq> findByRequestedBy_UserId(Long userId);

    /** 같은 컬럼에 이미 PENDING 상태(처리 대기중)인 확정 신청이 있는지 - 중복 신청 방지용. */
    boolean existsByAnlCol_ColumnIdAndRequestStatus(Long columnId, ReqStatCd requestStatus);

    /** 승인/반려/대기 전체 이력을 최신순으로 조회한다(관리자/승인자용 "처리 이력" 화면). */
    List<StdDmnReq> findAllByOrderByRequestedAtDesc();
}
