package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AnlJobLog;
import pf.cyj.sys.entity.type.ExecStatCd;

/** 배치작업 실행 이력(ANALYSIS_JOB_LOG) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlJobLogRsp {

    @Schema(description = "실행 이력 ID", example = "1")
    private Long logId;

    @Schema(description = "배치작업 ID", example = "1")
    private Long jobId;

    @Schema(description = "배치작업명", example = "야간 재판별 배치")
    private String jobName;

    @Schema(description = "실행 상태 - READY/RUNNING/SUCCESS/INTERNAL_ERROR/CALL_ERROR", example = "SUCCESS")
    private ExecStatCd execStatus;

    @Schema(description = "실행 시작 일시", example = "2026-08-26T02:00:00")
    private LocalDateTime startedAt;

    @Schema(description = "실행 종료 일시", example = "2026-08-26T02:05:00")
    private LocalDateTime endedAt;

    @Schema(description = "성공 처리 건수", example = "120")
    private Integer successCount;

    @Schema(description = "실패 처리 건수", example = "0")
    private Integer failCount;

    @Schema(description = "오류 메시지 (실패한 경우)", example = "외부 AI API 타임아웃")
    private String errorMessage;

    @Schema(description = "실행자 이름", example = "최윤정")
    private String executedByName;

    public static AnlJobLogRsp from(AnlJobLog log) {
        return new AnlJobLogRsp(
                log.getLogId(),
                log.getAnlJob() != null ? log.getAnlJob().getJobId() : null,
                log.getAnlJob() != null ? log.getAnlJob().getJobName() : null,
                log.getExecStatus(),
                log.getStartedAt(),
                log.getEndedAt(),
                log.getSuccessCount(),
                log.getFailCount(),
                log.getErrorMessage(),
                log.getExecutedBy() != null ? log.getExecutedBy().getUserName() : null
        );
    }
}
