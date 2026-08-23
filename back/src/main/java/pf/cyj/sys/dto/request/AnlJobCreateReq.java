package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** ANALYSIS_JOB 등록 요청 (분석/재판별 배치) */
public record AnlJobCreateReq(

        @NotBlank(message = "작업명은 필수입니다.")
        @Size(max = 200)
        String jobName,

        @Size(max = 20)
        String jobType,

        String datasetId,

        @NotBlank(message = "스케줄 타입은 필수입니다.")
        String scheduleType,

        @Size(max = 50)
        String cronExpr
) {
}
