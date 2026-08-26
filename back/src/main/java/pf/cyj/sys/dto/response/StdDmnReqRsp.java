package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.StdDmnReq;
import pf.cyj.sys.entity.type.ReqStatCd;

/** 표준 도메인 확정 신청(STANDARD_DOMAIN_REQUEST) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StdDmnReqRsp {

    @Schema(description = "신청 ID", example = "1")
    private Long requestId;

    @Schema(description = "신청 대상 컬럼 ID", example = "1")
    private Long columnId;

    @Schema(description = "신청 대상 컬럼명", example = "PHONE_NO")
    private String columnName;

    @Schema(description = "제안한 표준 도메인 코드", example = "CONTACT")
    private String proposedDomainCode;

    @Schema(description = "제안한 표준 도메인 한글명", example = "연락처")
    private String proposedDomainNameKo;

    @Schema(description = "AI 추천 결과를 그대로 채택했는지 여부", example = "true")
    private boolean aiSuggestedYn;

    @Schema(description = "신청 상태 - PENDING(대기)/APPROVED(승인)/REJECTED(반려)", example = "PENDING")
    private ReqStatCd requestStatus;

    @Schema(description = "신청자 이름", example = "최윤정")
    private String requestedByName;

    @Schema(description = "신청 일시", example = "2026-08-26T09:00:00")
    private LocalDateTime requestedAt;

    @Schema(description = "심사자 이름 (심사 완료 후에만 값이 있음)", example = "관리자")
    private String reviewedByName;

    @Schema(description = "심사 일시 (심사 완료 후에만 값이 있음)", example = "2026-08-26T10:00:00")
    private LocalDateTime reviewedAt;

    @Schema(description = "반려 사유 (반려된 경우에만 값이 있음)", example = "제안된 도메인이 실제 데이터 형식과 일치하지 않습니다.")
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
