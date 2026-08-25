package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ANALYSIS_COLUMN 단건 등록 요청 (AI 판별 입력 피처) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlColCreateReq {

    @NotBlank(message = "컬럼명은 필수입니다.")
    @Size(max = 100)
    private String columnName;

    @Size(max = 200)
    private String columnNameKo;

    @Size(max = 200)
    private String columnNameEn;

    @Size(max = 30)
    private String dataType;

    private Integer dataLength;

    private Integer dataScale;

    @NotNull
    private Boolean numericYn;

    @NotNull
    private Boolean dateYn;

    @NotNull
    private Boolean uniqueYn;
}
