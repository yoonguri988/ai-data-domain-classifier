package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 리포트 출력 이력(REPORT_EXPORT_LOG) 등록 요청 - PDF 판별결과 리포트를 출력/내보내기했다는 이력을 기록한다.
 * 실제 PDF 생성(PDFBox)은 이후 외부 연동 단계에서 붙고, 지금은 이력만 남긴다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RptExpLogCreateReq {

    @Schema(description = "리포트를 출력한 데이터셋 ID", example = "DS0000000001")
    @NotBlank(message = "데이터셋 ID는 필수입니다.")
    private String datasetId;

    @Schema(description = "출력한 사용자 ID", example = "1")
    @NotNull(message = "출력자 ID는 필수입니다.")
    private Long exportedById;

    @Schema(description = "출력된 파일명", example = "domain-prediction-report.pdf")
    @NotBlank(message = "파일명은 필수입니다.")
    @Size(max = 200)
    private String fileName;
}
