package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ANALYSIS_DATASET 등록 요청. DATASET_ID/REQUEST_NO 는 서비스단 BizIdGenerator 가 채번한다 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlDsetCreateReq {

    @NotBlank(message = "데이터셋명은 필수입니다.")
    @Size(max = 200)
    private String datasetName;

    @NotBlank(message = "DB 스키마명은 필수입니다.")
    @Size(max = 100)
    private String dbSchemaName;

    @NotBlank(message = "테이블명은 필수입니다.")
    @Size(max = 100)
    private String tableName;

    @Size(max = 10)
    private String dbmsTypeCode;
}
