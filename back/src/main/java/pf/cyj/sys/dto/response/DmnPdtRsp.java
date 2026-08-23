package pf.cyj.sys.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pf.cyj.sys.entity.DmnPdt;

/** AI(외부 API) 기반 컬럼별 표준 도메인 추천 결과 응답 */
public record DmnPdtRsp(
        Long predictionId,
        Long columnId,
        String columnName,
        String domainCode,
        String domainNameKo,
        Integer predictionRank,
        BigDecimal probability,
        String aiModelName,
        String aiModelVersion,
        Integer responseMs,
        boolean cacheHitYn,
        LocalDateTime predictedAt
) {
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
