package pf.cyj.sys.dto.request;

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

/** DOMAIN_PREDICTION 등록 요청 - 외부 AI API(AiDomainClassifier) 응답을 저장할 때 사용 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DmnPdtCreateReq {

    @NotNull(message = "컬럼 ID는 필수입니다.")
    private Long columnId;

    @NotBlank(message = "도메인 코드는 필수입니다.")
    private String domainCode;

    @NotNull(message = "추천 순위는 필수입니다.")
    @Positive
    private Integer predictionRank;

    @NotNull(message = "확률값은 필수입니다.")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private BigDecimal probability;

    @NotBlank(message = "AI 모델명은 필수입니다.")
    private String aiModelName;

    private String aiModelVersion;

    private Integer responseMs;

    @NotNull
    private Boolean cacheHitYn;
}
