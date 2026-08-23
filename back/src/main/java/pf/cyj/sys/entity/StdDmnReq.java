package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pf.cyj.sys.entity.converter.YnConverter;
import pf.cyj.sys.entity.type.ReqStatCd;

/** STANDARD_DOMAIN_REQUEST - 표준 도메인 확정 신청(신청본 성격). 담당자 승인 대기열 */
@Entity
@Table(name = "STANDARD_DOMAIN_REQUEST")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StdDmnReq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REQUEST_ID")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLUMN_ID", nullable = false)
    private AnlCol anlCol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROPOSED_DOMAIN_CODE", nullable = false)
    private DmnCd proposedDmnCd;

    /** AI 1순위 추천을 그대로 채택했는지 여부 - 이후 AI 추천 채택률 집계에 사용 */
    @Convert(converter = YnConverter.class)
    @Column(name = "AI_SUGGESTED_YN", nullable = false, length = 1, columnDefinition = "char(1)")
    @Builder.Default
    private boolean aiSuggestedYn = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "REQUEST_STATUS", nullable = false, length = 10)
    @Builder.Default
    private ReqStatCd requestStatus = ReqStatCd.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTED_BY", nullable = false)
    private AppUsr requestedBy;

    @CreationTimestamp
    @Column(name = "REQUESTED_AT", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEWED_BY")
    private AppUsr reviewedBy;

    @Column(name = "REVIEWED_AT")
    private LocalDateTime reviewedAt;

    @Column(name = "REJECT_REASON", length = 500)
    private String rejectReason;
}
