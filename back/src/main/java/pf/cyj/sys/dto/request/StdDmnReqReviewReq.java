package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 표준 도메인 확정 신청 승인/반려 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StdDmnReqReviewReq {

    @NotNull(message = "승인 여부는 필수입니다.")
    private Boolean approve;

    @Size(max = 500)
    private String rejectReason;
}
