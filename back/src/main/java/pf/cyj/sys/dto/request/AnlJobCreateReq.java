package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ANALYSIS_JOB 등록 요청 (분석/재판별 배치) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlJobCreateReq {

    @NotBlank(message = "작업명은 필수입니다.")
    @Size(max = 200)
    private String jobName;

    @Size(max = 20)
    private String jobType;

    private String datasetId;

    @NotBlank(message = "스케줄 타입은 필수입니다.")
    private String scheduleType;

    @Size(max = 50)
    private String cronExpr;
}
