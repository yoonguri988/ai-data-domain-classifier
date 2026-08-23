package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import pf.cyj.sys.entity.AnlJob;
import pf.cyj.sys.entity.type.JobStatCd;
import pf.cyj.sys.entity.type.SchdTypCd;

public record AnlJobRsp(
        Long jobId,
        String jobName,
        String jobType,
        String datasetId,
        SchdTypCd scheduleType,
        String cronExpr,
        JobStatCd jobStatus,
        String createdByName,
        LocalDateTime createdAt
) {
    public static AnlJobRsp from(AnlJob job) {
        return new AnlJobRsp(
                job.getJobId(),
                job.getJobName(),
                job.getJobType(),
                job.getAnlDset() != null ? job.getAnlDset().getDatasetId() : null,
                job.getScheduleType(),
                job.getCronExpr(),
                job.getJobStatus(),
                job.getCreatedBy() != null ? job.getCreatedBy().getUserName() : null,
                job.getCreatedAt()
        );
    }
}
