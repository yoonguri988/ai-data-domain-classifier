package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotNull;

/** 표준 도메인 확정 신청 요청 (AI 추천 결과를 그대로 채택하거나 담당자가 직접 수정해서 신청) */
public record StdDmnReqCreateReq(

        @NotNull(message = "컬럼 ID는 필수입니다.")
        Long columnId,

        @NotNull(message = "제안 도메인 코드는 필수입니다.")
        String proposedDomainCode,

        @NotNull(message = "AI 추천 채택 여부는 필수입니다.")
        Boolean aiSuggestedYn
) {
}
