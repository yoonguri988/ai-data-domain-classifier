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
import pf.cyj.sys.entity.type.ExecStatCd;

/**
 * ANALYSIS_JOB_LOG - 배치 작업 실행 이력.
 * 템플릿 메서드 패턴(시작 로그 → 실행 → 성공/실패 기록)을 서비스 계층(AbstractAnlJobExecutor)에서 유지한다.
 */
@Entity
@Table(name = "ANALYSIS_JOB_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnlJobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_ID", nullable = false)
    private AnlJob anlJob;

    /** READY/RUNNING/SUCCESS/INTERNAL_ERROR/CALL_ERROR (5종 상태 유지) */
    @Enumerated(EnumType.STRING)
    @Column(name = "EXEC_STATUS", nullable = false, length = 20)
    @Builder.Default
    private ExecStatCd execStatus = ExecStatCd.READY;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    @Column(name = "ENDED_AT")
    private LocalDateTime endedAt;

    @Column(name = "SUCCESS_COUNT")
    @Builder.Default
    private Integer successCount = 0;

    @Column(name = "FAIL_COUNT")
    @Builder.Default
    private Integer failCount = 0;

    @Column(name = "ERROR_MESSAGE", length = 1000)
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXECUTED_BY")
    private AppUsr executedBy;
}
