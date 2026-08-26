package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.DmnPdt;

/** AI(외부 API) 기반 컬럼별 표준 도메인 추천 결과 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DmnPdtRsp {

    @Schema(description = "판별 결과 ID", example = "1")
    private Long predictionId;

    @Schema(description = "판별 대상 컬럼 ID", example = "1")
    private Long columnId;

    @Schema(description = "판별 대상 컬럼명", example = "PHONE_NO")
    private String columnName;

    @Schema(description = "추천된 표준 도메인 코드", example = "DOM_PHONE")
    private String domainCode;

    @Schema(description = "추천된 표준 도메인 한글명", example = "전화번호")
    private String domainNameKo;

    @Schema(description = "추천 순위 (1이 가장 유력)", example = "1")
    private Integer predictionRank;

    @Schema(description = "판별 확률 (0.0 ~ 1.0)", example = "0.87")
    private BigDecimal probability;

    @Schema(description = "사용된 AI 모델명", example = "claude-haiku")
    private String aiModelName;

    @Schema(description = "AI 모델 버전", example = "4.5")
    private String aiModelVersion;

    @Schema(description = "응답 소요 시간(ms)", example = "320")
    private Integer responseMs;

    @Schema(description = "캐시 응답 여부", example = "false")
    private boolean cacheHitYn;

    @Schema(description = "판별 일시", example = "2026-08-26T09:00:00")
    private LocalDateTime predictedAt;

    public static DmnPdtRsp from(DmnPdt pdt) {
        return new DmnPdtRsp(
                pdt.getPredictionId(),
                pdt.getAnlCol() != null ? pdt.getAnlCol().getColumnId() : null,
                pdt.getAnlCol() != null ? pdt.getAnlCol().getColumnName() : null,
                pdt.getDmnCd() != null ? pdt.getDmnCd().getDomainCode() : null,
                pdt.getDmnCd() != null ? pdt.getDmnCd().getDomainNameKo() : null,
                pdt.getPredictionRank(),
                pdt.getProbability(),
                pdt.getAiModelName(),
                pdt.getAiModelVersion(),
                pdt.getResponseMs(),
                pdt.isCacheHitYn(),
                pdt.getPredictedAt()
        );
    }
}
