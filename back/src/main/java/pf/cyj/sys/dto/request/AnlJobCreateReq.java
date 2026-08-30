package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배치작업(ANALYSIS_JOB) 등록 요청 - 분석/재판별 배치를 예약한다 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlJobCreateReq {

    @Schema(description = "배치작업명", example = "야간 재판별 배치")
    @NotBlank(message = "작업명은 필수입니다.")
    @Size(max = 200)
    private String jobName;

    @Schema(description = "작업 유형 (선택)", example = "DOMAIN_PREDICT")
    @Size(max = 20)
    private String jobType;

    @Schema(description = "대상 데이터셋 ID (선택, 전체 데이터셋 대상이면 비워둔다)", example = "DS0000000001")
    private String datasetId;

    @Schema(description = "스케줄 타입 - ONCE(1회 실행) 또는 CRON(주기 실행)", example = "ONCE", allowableValues = {"ONCE", "CRON"})
    @NotBlank(message = "스케줄 타입은 필수입니다.")
    private String scheduleType;

    @Schema(description = "CRON 표현식 (scheduleType=CRON 일 때만 사용, 선택)", example = "0 0 2 * * *")
    @Size(max = 50)
    private String cronExpr;
}
