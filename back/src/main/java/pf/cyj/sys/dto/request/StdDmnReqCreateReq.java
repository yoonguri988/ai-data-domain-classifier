package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 표준 도메인 확정 신청 요청 - AI 추천 결과를 그대로 채택하거나 담당자가 직접 수정해서 신청한다 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StdDmnReqCreateReq {

    @Schema(description = "확정을 신청할 컬럼 ID", example = "1")
    @NotNull(message = "컬럼 ID는 필수입니다.")
    private Long columnId;

    @Schema(description = "제안하는 표준 도메인 코드", example = "DOM_PHONE")
    @NotNull(message = "제안 도메인 코드는 필수입니다.")
    private String proposedDomainCode;

    @Schema(description = "AI 추천 결과를 그대로 채택했는지 여부", example = "true")
    @NotNull(message = "AI 추천 채택 여부는 필수입니다.")
    private Boolean aiSuggestedYn;
}
