package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.StdDmnReq;
import pf.cyj.sys.entity.type.ReqStatCd;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StdDmnReqRsp {

    private Long requestId;
    private Long columnId;
    private String columnName;
    private String proposedDomainCode;
    private String proposedDomainNameKo;
    private boolean aiSuggestedYn;
    private ReqStatCd requestStatus;
    private String requestedByName;
    private LocalDateTime requestedAt;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String rejectReason;

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
