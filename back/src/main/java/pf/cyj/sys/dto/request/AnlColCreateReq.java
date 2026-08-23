package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ANALYSIS_COLUMN 단건 등록 요청 (AI 판별 입력 피처) */
public record AnlColCreateReq(

        @NotBlank(message = "컬럼명은 필수입니다.")
        @Size(max = 100)
        String columnName,

        @Size(max = 200)
        String columnNameKo,

        @Size(max = 200)
        String columnNameEn,

        @Size(max = 30)
        String dataType,

        Integer dataLength,

        Integer dataScale,

        @NotNull
        Boolean numericYn,

        @NotNull
        Boolean dateYn,

        @NotNull
        Boolean uniqueYn
) {
}
