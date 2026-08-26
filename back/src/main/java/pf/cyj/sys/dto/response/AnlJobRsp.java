package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AnlJob;
import pf.cyj.sys.entity.type.JobStatCd;
import pf.cyj.sys.entity.type.SchdTypCd;

/** 배치작업(ANALYSIS_JOB) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlJobRsp {

    @Schema(description = "배치작업 ID", example = "1")
    private Long jobId;

    @Schema(description = "배치작업명", example = "야간 재판별 배치")
    private String jobName;

    @Schema(description = "작업 유형", example = "REDETECT")
    private String jobType;

    @Schema(description = "대상 데이터셋 ID", example = "DS_00000001")
    private String datasetId;

    @Schema(description = "스케줄 타입 - ONCE(1회)/CRON(주기)", example = "ONCE")
    private SchdTypCd scheduleType;

    @Schema(description = "CRON 표현식 (scheduleType=CRON 일 때만 값이 있음)", example = "0 0 2 * * *")
    private String cronExpr;

    @Schema(description = "작업 상태 - READY/RUNNING/SUCCESS/FAILED/DISABLED", example = "READY")
    private JobStatCd jobStatus;

    @Schema(description = "등록자 이름", example = "최윤정")
    private String createdByName;

    @Schema(description = "등록일시", example = "2026-08-26T09:00:00")
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
