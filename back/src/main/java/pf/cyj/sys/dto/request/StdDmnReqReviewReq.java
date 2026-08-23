package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 표준 도메인 확정 신청 승인/반려 요청 */
public record StdDmnReqReviewReq(

        @NotNull(message = "승인 여부는 필수입니다.")
        Boolean approve,

        @Size(max = 500)
        String rejectReason
) {
}
