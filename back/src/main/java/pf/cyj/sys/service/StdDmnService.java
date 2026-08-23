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

/** 표준도메인승인 - 신청(신청본) 등록/승인/반려, 확정(확정본) 조회 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StdDmnService {

    private final AppUsrRepository appUsrRepository;
    private final AnlColRepository anlColRepository;
    private final DmnCdRepository dmnCdRepository;
    private final StdDmnReqRepository stdDmnReqRepository;
    private final StdDmnRepository stdDmnRepository;

    @Transactional
    public StdDmnReqRsp applyRequest(StdDmnReqCreateReq req, Long requesterId) {
        AnlCol col = anlColRepository.findById(req.columnId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 컬럼입니다: " + req.columnId()));
        DmnCd domain = dmnCdRepository.findById(req.proposedDomainCode())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 도메인 코드입니다: " + req.proposedDomainCode()));
        AppUsr requester = appUsrRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다: " + requesterId));

        StdDmnReq entity = StdDmnReq.builder()
                .anlCol(col)
                .proposedDmnCd(domain)
                .aiSuggestedYn(Boolean.TRUE.equals(req.aiSuggestedYn()))
                .requestedBy(requester)
                .build();

        return StdDmnReqRsp.from(stdDmnReqRepository.save(entity));
    }

    public List<StdDmnReqRsp> findPendingRequests() {
        return stdDmnReqRepository.findByRequestStatusOrderByRequestedAtAsc(ReqStatCd.PENDING)
                .stream().map(StdDmnReqRsp::from).toList();
    }

    public List<StdDmnReqRsp> findRequestsByRequester(Long requesterId) {
        return stdDmnReqRepository.findByRequestedBy_UserId(requesterId).stream().map(StdDmnReqRsp::from).toList();
    }

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

        if (Boolean.TRUE.equals(req.approve())) {
            entity.setRequestStatus(ReqStatCd.APPROVED);
            confirmDomain(entity, reviewer);
        } else {
            entity.setRequestStatus(ReqStatCd.REJECTED);
            entity.setRejectReason(req.rejectReason());
        }

        return StdDmnReqRsp.from(entity);
    }

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
