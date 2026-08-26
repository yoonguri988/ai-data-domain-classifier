package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 분석대상 데이터셋(ANALYSIS_DATASET) 등록 요청.
 * datasetId(업무키)/requestNo 는 요청값으로 받지 않고 서비스단 BizIdGenerator 가 자동 채번한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlDsetCreateReq {

    @Schema(description = "데이터셋명", example = "고객 마스터 테이블")
    @NotBlank(message = "데이터셋명은 필수입니다.")
    @Size(max = 200)
    private String datasetName;

    @Schema(description = "분석 대상 DB 스키마명", example = "SALES")
    @NotBlank(message = "DB 스키마명은 필수입니다.")
    @Size(max = 100)
    private String dbSchemaName;

    @Schema(description = "분석 대상 테이블명", example = "TB_CUSTOMER")
    @NotBlank(message = "테이블명은 필수입니다.")
    @Size(max = 100)
    private String tableName;

    @Schema(description = "DBMS 종류 코드 (선택, 예: ORACLE)", example = "ORACLE")
    @Size(max = 10)
    private String dbmsTypeCode;
}
