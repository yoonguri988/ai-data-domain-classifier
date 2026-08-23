package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import pf.cyj.sys.entity.StdDmnReq;
import pf.cyj.sys.entity.type.ReqStatCd;

public record StdDmnReqRsp(
        Long requestId,
        Long columnId,
        String columnName,
        String proposedDomainCode,
        String proposedDomainNameKo,
        boolean aiSuggestedYn,
        ReqStatCd requestStatus,
        String requestedByName,
        LocalDateTime requestedAt,
        String reviewedByName,
        LocalDateTime reviewedAt,
        String rejectReason
) {
    public static StdDmnReqRsp from(StdDmnReq req) {
        return new StdDmnReqRsp(
                req.getRequestId(),
                req.getAnlCol() != null ? req.getAnlCol().getColumnId() : null,
                req.getAnlCol() != null ? req.getAnlCol().getColumnName() : null,
                req.getProposedDmnCd() != null ? req.getProposedDmnCd().getDomainCode() : null,
                req.getProposedDmnCd() != null ? req.getProposedDmnCd().getDomainNameKo() : null,
                req.isAiSuggestedYn(),
                req.getRequestStatus(),
                req.getRequestedBy() != null ? req.getRequestedBy().getUserName() : null,
                req.getRequestedAt(),
                req.getReviewedBy() != null ? req.getReviewedBy().getUserName() : null,
                req.getReviewedAt(),
                req.getRejectReason()
        );
    }
}
