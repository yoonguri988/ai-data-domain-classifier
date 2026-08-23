package pf.cyj.sys.entity;

import jakarta.persistence.Column;
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
import pf.cyj.sys.entity.type.JobStatCd;
import pf.cyj.sys.entity.type.SchdTypCd;

/** ANALYSIS_JOB - 분석/재판별 배치 작업 등록 (구 Quartz 등록 화면 대체) */
@Entity
@Table(name = "ANALYSIS_JOB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnlJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_ID")
    private Long jobId;

    @Column(name = "JOB_NAME", nullable = false, length = 200)
    private String jobName;

    @Column(name = "JOB_TYPE", nullable = false, length = 20)
    @Builder.Default
    private String jobType = "DOMAIN_PREDICT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATASET_ID")
    private AnlDset anlDset;

    @Enumerated(EnumType.STRING)
    @Column(name = "SCHEDULE_TYPE", nullable = false, length = 10)
    @Builder.Default
    private SchdTypCd scheduleType = SchdTypCd.ONCE;

    @Column(name = "CRON_EXPR", length = 50)
    private String cronExpr;

    @Enumerated(EnumType.STRING)
    @Column(name = "JOB_STATUS", nullable = false, length = 20)
    @Builder.Default
    private JobStatCd jobStatus = JobStatCd.READY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY", nullable = false)
    private AppUsr createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
