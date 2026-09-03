package pf.cyj.sys.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.dto.request.StdDmnReqCreateReq;
import pf.cyj.sys.dto.request.StdDmnReqReviewReq;
import pf.cyj.sys.dto.response.StdDmnReqRsp;
import pf.cyj.sys.dto.response.StdDmnRsp;
import pf.cyj.sys.entity.AnlCol;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.DmnCd;
import pf.cyj.sys.entity.StdDmn;
import pf.cyj.sys.entity.StdDmnReq;
import pf.cyj.sys.entity.type.ReqStatCd;
import pf.cyj.sys.exception.BizRuleException;
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.repository.AnlColRepository;
import pf.cyj.sys.repository.AppUsrRepository;
import pf.cyj.sys.repository.DmnCdRepository;
import pf.cyj.sys.repository.StdDmnRepository;
import pf.cyj.sys.repository.StdDmnReqRepository;

/**
 * 표준도메인승인 - 신청(신청본) 등록/승인/반려, 확정(확정본) 조회.
 * 승인/반려가 결정되면 신청자에게 이메일 알림을 자동 발송한다(NotiRptService, v4.7).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StdDmnService {

    private final AppUsrRepository appUsrRepository;
    private final AnlColRepository anlColRepository;
    private final DmnCdRepository dmnCdRepository;
    private final StdDmnReqRepository stdDmnReqRepository;
    private final StdDmnRepository stdDmnRepository;
    private final NotiRptService notiRptService;

    /** 표준 도메인 확정을 신청한다(상태는 PENDING 으로 시작해 관리자 승인/반려를 기다린다). */
    @Transactional
    public StdDmnReqRsp applyRequest(StdDmnReqCreateReq req, Long requesterId) {
        // 이미 같은 컬럼에 처리 대기(PENDING) 중인 신청이 있으면 또 신청할 수 없게 막는다 - 막지
        // 않으면 승인자가 같은 컬럼에 대한 신청을 여러 건 보게 되고, 그중 하나만 승인해도 나머지
        // PENDING 건은 그대로 남아 있어 확정본(StdDmn) 상태와 어긋나 보일 수 있다. 이미 CONFIRMED된
        // (승인 완료된) 컬럼에 대한 재신청까지 막지는 않는다 - review()의 confirmDomain()이 versionNo를
        // 올리며 재확정을 허용하는 게 이 프로젝트의 의도된 설계이기 때문이다(예: 표준 도메인을 다시
        // 검토해 다른 도메인으로 재분류하는 경우).
        if (stdDmnReqRepository.existsByAnlCol_ColumnIdAndRequestStatus(req.getColumnId(), ReqStatCd.PENDING)) {
            throw new BizRuleException(
                    "DUPLICATE_PENDING_REQUEST",
                    "이미 처리 대기 중인 확정 신청이 있습니다. columnId=" + req.getColumnId());
        }

        AnlCol col = anlColRepository.findById(req.getColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 컬럼입니다: " + req.getColumnId()));
        DmnCd domain = dmnCdRepository.findById(req.getProposedDomainCode())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 도메인 코드입니다: " + req.getProposedDomainCode()));
        AppUsr requester = appUsrRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다: " + requesterId));

        StdDmnReq entity = StdDmnReq.builder()
                .anlCol(col)
                .proposedDmnCd(domain)
                .aiSuggestedYn(Boolean.TRUE.equals(req.getAiSuggestedYn()))
                .requestedBy(requester)
                .build();

        return StdDmnReqRsp.from(stdDmnReqRepository.save(entity));
    }

    /** 아직 승인/반려 처리되지 않은(PENDING) 확정 신청 목록을 조회한다. */
    public List<StdDmnReqRsp> findPendingRequests() {
        return stdDmnReqRepository.findByRequestStatusOrderByRequestedAtAsc(ReqStatCd.PENDING)
                .stream().map(StdDmnReqRsp::from).toList();
    }

    /** 특정 사용자가 신청한 확정 요청 이력을 조회한다. */
    public List<StdDmnReqRsp> findRequestsByRequester(Long requesterId) {
        return stdDmnReqRepository.findByRequestedBy_UserId(requesterId).stream().map(StdDmnReqRsp::from).toList();
    }

    /**
     * PENDING/APPROVED/REJECTED 를 가리지 않고 전체 확정 신청 이력을 최신순으로 조회한다(관리자/승인자용
     * "처리 이력" 화면 - findPendingRequests() 는 PENDING 만 보여주므로 이미 승인/반려된 건은 이 메서드로
     * 봐야 한다).
     */
    public List<StdDmnReqRsp> findAllRequests() {
        return stdDmnReqRepository.findAllByOrderByRequestedAtDesc().stream().map(StdDmnReqRsp::from).toList();
    }

    /** 대기중인 확정 신청을 승인 또는 반려 처리한다. 승인 시 표준 도메인 확정본(StdDmn)이 생성/갱신된다. */
    @Transactional
    public StdDmnReqRsp review(Long requestId, StdDmnReqReviewReq req, Long reviewerId) {
        StdDmnReq entity = stdDmnReqRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 신청 건입니다: " + requestId));

        if (entity.getRequestStatus() != ReqStatCd.PENDING) {
            throw new BizRuleException("ALREADY_REVIEWED", "이미 처리된 신청 건입니다: " + requestId);
        }

        AppUsr reviewer = appUsrRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다: " + reviewerId));

        entity.setReviewedBy(reviewer);
        entity.setReviewedAt(LocalDateTime.now());

        String title;
        String content;
        if (Boolean.TRUE.equals(req.getApprove())) {
            entity.setRequestStatus(ReqStatCd.APPROVED);
            confirmDomain(entity, reviewer);
            title = "표준 도메인 확정 승인 완료";
            content = "[" + entity.getAnlCol().getColumnName() + "] 컬럼의 표준 도메인이 '"
                    + entity.getProposedDmnCd().getDomainNameKo() + "'(으)로 확정되었습니다.";
        } else {
            entity.setRequestStatus(ReqStatCd.REJECTED);
            entity.setRejectReason(req.getRejectReason());
            title = "표준 도메인 확정 신청 반려";
            content = "[" + entity.getAnlCol().getColumnName() + "] 컬럼의 표준 도메인 확정 신청이 반려되었습니다. 사유: "
                    + req.getRejectReason();
        }

        // 알림 발송이 실패해도(예: 메일 서버 오류) 승인/반려 자체는 이미 확정됐으니 롤백하지 않는다 -
        // sendAndRecord() 내부에서 실패를 예외로 던지지 않고 SEND_STATUS=FAIL 로만 기록하는 이유다.
        notiRptService.sendAndRecord(entity.getRequestedBy(), entity, title, content);

        return StdDmnReqRsp.from(entity);
    }

    /** 컬럼에 확정된 표준 도메인(확정본)을 조회한다. */
    public StdDmnRsp findConfirmedByColumn(Long columnId) {
        return stdDmnRepository.findByAnlCol_ColumnId(columnId)
                .map(StdDmnRsp::from)
                .orElseThrow(() -> new ResourceNotFoundException("확정된 표준 도메인이 없습니다. columnId=" + columnId));
    }

    private void confirmDomain(StdDmnReq approvedReq, AppUsr reviewer) {
        StdDmn confirmed = stdDmnRepository.findByAnlCol_ColumnId(approvedReq.getAnlCol().getColumnId())
                .map(existing -> {
                    existing.setDmnCd(approvedReq.getProposedDmnCd());
                    existing.setVersionNo(existing.getVersionNo() + 1);
                    existing.setConfirmedBy(reviewer);
                    return existing;
                })
                .orElseGet(() -> StdDmn.builder()
                        .anlCol(approvedReq.getAnlCol())
                        .dmnCd(approvedReq.getProposedDmnCd())
                        .confirmedBy(reviewer)
                        .build());

        stdDmnRepository.save(confirmed);
    }
}
