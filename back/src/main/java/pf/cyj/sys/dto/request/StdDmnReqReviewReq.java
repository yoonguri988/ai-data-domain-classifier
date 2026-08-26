package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 표준 도메인 확정 신청 승인/반려 요청 (ROLE_ADMIN 전용) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StdDmnReqReviewReq {

    @Schema(description = "승인 여부 - true 면 승인(확정본 생성/갱신), false 면 반려", example = "true")
    @NotNull(message = "승인 여부는 필수입니다.")
    private Boolean approve;

    @Schema(description = "반려 사유 (approve=false 일 때 입력)", example = "제안된 도메인이 실제 데이터 형식과 일치하지 않습니다.")
    @Size(max = 500)
    private String rejectReason;
}
