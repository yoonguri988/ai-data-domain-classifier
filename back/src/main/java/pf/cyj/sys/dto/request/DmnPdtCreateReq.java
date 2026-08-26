package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI 판별 결과(DOMAIN_PREDICTION) 등록 요청.
 * 외부 AI API(AiDomainClassifier) 호출은 아직 붙지 않았고, 이미 계산된 판별 결과를 저장할 때 사용한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DmnPdtCreateReq {

    @Schema(description = "판별 대상 컬럼 ID", example = "1")
    @NotNull(message = "컬럼 ID는 필수입니다.")
    private Long columnId;

    @Schema(description = "추천된 표준 도메인 코드", example = "CONTACT")
    @NotBlank(message = "도메인 코드는 필수입니다.")
    private String domainCode;

    @Schema(description = "추천 순위 (1이 가장 유력)", example = "1")
    @NotNull(message = "추천 순위는 필수입니다.")
    @Positive
    private Integer predictionRank;

    @Schema(description = "판별 확률 (0.0 ~ 1.0)", example = "0.87")
    @NotNull(message = "확률값은 필수입니다.")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private BigDecimal probability;

    @Schema(description = "사용한 AI 모델명", example = "claude-haiku")
    @NotBlank(message = "AI 모델명은 필수입니다.")
    private String aiModelName;

    @Schema(description = "AI 모델 버전 (선택)", example = "4.5")
    private String aiModelVersion;

    @Schema(description = "응답 소요 시간(ms, 선택)", example = "320")
    private Integer responseMs;

    @Schema(description = "캐시 응답 여부", example = "false")
    @NotNull
    private Boolean cacheHitYn;
}
