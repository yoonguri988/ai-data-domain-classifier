package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pf.cyj.sys.entity.converter.YnConverter;

/** DOMAIN_PREDICTION - AI(외부 API) 기반 컬럼별 표준 도메인 추천 결과 Top-N */
@Entity
@Table(name = "DOMAIN_PREDICTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmnPdt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PREDICTION_ID")
    private Long predictionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLUMN_ID", nullable = false)
    private AnlCol anlCol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOMAIN_CODE", nullable = false)
    private DmnCd dmnCd;

    /** 1=가장 유력한 후보 */
    @Column(name = "PREDICTION_RANK", nullable = false)
    private Integer predictionRank;

    /** 0.00000 ~ 1.00000 */
    @Column(name = "PROBABILITY", nullable = false, precision = 6, scale = 5)
    private BigDecimal probability;

    /** 예: gpt-4o-mini, claude-haiku, self-hosted-classifier */
    @Column(name = "AI_MODEL_NAME", nullable = false, length = 50)
    private String aiModelName;

    @Column(name = "AI_MODEL_VERSION", length = 20)
    private String aiModelVersion;

    /** 외부 AI API 응답시간(ms) - 성능 개선 지표로 활용 */
    @Column(name = "RESPONSE_MS")
    private Integer responseMs;

    @Convert(converter = YnConverter.class)
    @Column(name = "CACHE_HIT_YN", nullable = false, length = 1, columnDefinition = "char(1)")
    @Builder.Default
    private boolean cacheHitYn = false;

    @CreationTimestamp
    @Column(name = "PREDICTED_AT", nullable = false, updatable = false)
    private LocalDateTime predictedAt;
}
