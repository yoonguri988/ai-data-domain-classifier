package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.RptExpLog;

/** 리포트 출력 이력(REPORT_EXPORT_LOG) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RptExpLogRsp {

    @Schema(description = "출력 이력 ID", example = "1")
    private Long exportId;

    @Schema(description = "출력한 데이터셋 ID", example = "DS0000000001")
    private String datasetId;

    @Schema(description = "출력된 파일명", example = "domain-prediction-report.pdf")
    private String fileName;

    @Schema(description = "출력한 사용자 이름", example = "최윤정")
    private String exportedByName;

    @Schema(description = "출력 일시", example = "2026-08-26T09:00:00")
    private LocalDateTime exportedAt;

    public static RptExpLogRsp from(RptExpLog log) {
        return new RptExpLogRsp(
                log.getExportId(),
                log.getAnlDset() != null ? log.getAnlDset().getDatasetId() : null,
                log.getFileName(),
                log.getExportedBy() != null ? log.getExportedBy().getUserName() : null,
                log.getExportedAt()
        );
    }
}
