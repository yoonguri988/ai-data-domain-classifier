package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 분석대상 컬럼(ANALYSIS_COLUMN) 단건 등록 요청 - AI 판별의 입력 피처가 되는 컬럼 메타정보 1개 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlColCreateReq {

    @Schema(description = "컬럼명 (실제 DB 컬럼명)", example = "PHONE_NO")
    @NotBlank(message = "컬럼명은 필수입니다.")
    @Size(max = 100)
    private String columnName;

    @Schema(description = "컬럼 한글명 (선택)", example = "전화번호")
    @Size(max = 200)
    private String columnNameKo;

    @Schema(description = "컬럼 영문명 (선택)", example = "Phone Number")
    @Size(max = 200)
    private String columnNameEn;

    @Schema(description = "DB 데이터 타입 (선택)", example = "VARCHAR2")
    @Size(max = 30)
    private String dataType;

    @Schema(description = "데이터 길이 (선택)", example = "20")
    private Integer dataLength;

    @Schema(description = "데이터 소수 자릿수 (선택)", example = "0")
    private Integer dataScale;

    @Schema(description = "숫자형 컬럼 여부", example = "false")
    @NotNull
    private Boolean numericYn;

    @Schema(description = "날짜형 컬럼 여부", example = "false")
    @NotNull
    private Boolean dateYn;

    @Schema(description = "유니크(고유값) 컬럼 여부", example = "false")
    @NotNull
    private Boolean uniqueYn;
}
