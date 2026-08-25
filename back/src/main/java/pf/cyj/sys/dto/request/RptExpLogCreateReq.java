package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** REPORT_EXPORT_LOG 등록 요청 - PDF 판별결과 리포트 출력이력 기록 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RptExpLogCreateReq {

    @NotBlank(message = "데이터셋 ID는 필수입니다.")
    private String datasetId;

    @NotNull(message = "출력자 ID는 필수입니다.")
    private Long exportedById;

    @NotBlank(message = "파일명은 필수입니다.")
    @Size(max = 200)
    private String fileName;
}
