package pf.cyj.sys.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * STANDARD_DOMAIN - 승인 완료된 확정 표준 도메인(확정본 성격). 컬럼당 최신 1건.
 * ANALYSIS_COLUMN 과 PK(COLUMN_ID)를 공유하는 1:1 관계이므로 @MapsId 를 사용한다.
 */
@Entity
@Table(name = "STANDARD_DOMAIN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StdDmn {

    @Id
    @Column(name = "COLUMN_ID")
    private Long columnId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "COLUMN_ID")
    private AnlCol anlCol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOMAIN_CODE", nullable = false)
    private DmnCd dmnCd;

    @Column(name = "VERSION_NO", nullable = false)
    @Builder.Default
    private Integer versionNo = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONFIRMED_BY", nullable = false)
    private AppUsr confirmedBy;

    /** 컬럼당 최신 1건 upsert 구조이므로, 재승인 시(버전업) 최신 확정 시각으로 갱신되도록 @UpdateTimestamp 를 사용한다 */
    @UpdateTimestamp
    @Column(name = "CONFIRMED_AT", nullable = false)
    private LocalDateTime confirmedAt;
}
