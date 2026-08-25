package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AnlJob;
import pf.cyj.sys.entity.type.JobStatCd;
import pf.cyj.sys.entity.type.SchdTypCd;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlJobRsp {

    private Long jobId;
    private String jobName;
    private String jobType;
    private String datasetId;
    private SchdTypCd scheduleType;
    private String cronExpr;
    private JobStatCd jobStatus;
    private String createdByName;
    private LocalDateTime createdAt;

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
