package pf.cyj.sys.dto.response;

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

    private Long predictionId;
    private Long columnId;
    private String columnName;
    private String domainCode;
    private String domainNameKo;
    private Integer predictionRank;
    private BigDecimal probability;
    private String aiModelName;
    private String aiModelVersion;
    private Integer responseMs;
    private boolean cacheHitYn;
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
