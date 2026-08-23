package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import pf.cyj.sys.entity.type.DsetStatCd;

/**
 * ANALYSIS_DATASET - AI 도메인 판별 대상 데이터셋(테이블 단위) 등록.
 * DATASET_ID/REQUEST_NO 는 업무키(예: DS0000000001)로, DB 트리거가 아니라
 * service.support.BizIdGenerator 가 시퀀스를 조회해 애플리케이션에서 채번한다(JPA는 PK를 insert 이전에 알아야 하므로).
 */
@Entity
@Table(name = "ANALYSIS_DATASET")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnlDset {

    @Id
    @Column(name = "DATASET_ID", length = 20)
    private String datasetId;

    @Column(name = "REQUEST_NO", nullable = false, length = 20, unique = true)
    private String requestNo;

    @Column(name = "DATASET_NAME", nullable = false, length = 200)
    private String datasetName;

    @Column(name = "DB_SCHEMA_NAME", nullable = false, length = 100)
    private String dbSchemaName;

    @Column(name = "TABLE_NAME", nullable = false, length = 100)
    private String tableName;

    /** ORA/MYS/MSQ/PGS ... */
    @Column(name = "DBMS_TYPE_CODE", nullable = false, length = 10)
    @Builder.Default
    private String dbmsTypeCode = "ORA";

    @Enumerated(EnumType.STRING)
    @Column(name = "DATASET_STATUS", nullable = false, length = 15)
    @Builder.Default
    private DsetStatCd datasetStatus = DsetStatCd.REGISTERED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTED_BY", nullable = false)
    private AppUsr requestedBy;

    @CreationTimestamp
    @Column(name = "REQUESTED_AT", nullable = false, updatable = false)
    private LocalDateTime requestedAt;
}
