package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import pf.cyj.sys.entity.AnlJobLog;
import pf.cyj.sys.entity.type.ExecStatCd;

public record AnlJobLogRsp(
        Long logId,
        Long jobId,
        String jobName,
        ExecStatCd execStatus,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer successCount,
        Integer failCount,
        String errorMessage,
        String executedByName
) {
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
