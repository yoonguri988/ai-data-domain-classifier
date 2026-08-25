package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AnlJobLog;
import pf.cyj.sys.entity.type.ExecStatCd;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlJobLogRsp {

    private Long logId;
    private Long jobId;
    private String jobName;
    private ExecStatCd execStatus;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer successCount;
    private Integer failCount;
    private String errorMessage;
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
