package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AnlCol;

/** 분석대상 컬럼(ANALYSIS_COLUMN) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlColRsp {

    @Schema(description = "컬럼 ID", example = "1")
    private Long columnId;

    @Schema(description = "이 컬럼이 속한 데이터셋 ID", example = "DS_00000001")
    private String datasetId;

    @Schema(description = "컬럼명 (실제 DB 컬럼명)", example = "PHONE_NO")
    private String columnName;

    @Schema(description = "컬럼 한글명", example = "전화번호")
    private String columnNameKo;

    @Schema(description = "컬럼 영문명", example = "Phone Number")
    private String columnNameEn;

    @Schema(description = "DB 데이터 타입", example = "VARCHAR2")
    private String dataType;

    @Schema(description = "데이터 길이", example = "20")
    private Integer dataLength;

    @Schema(description = "데이터 소수 자릿수", example = "0")
    private Integer dataScale;

    @Schema(description = "숫자형 컬럼 여부", example = "false")
    private boolean numericYn;

    @Schema(description = "날짜형 컬럼 여부", example = "false")
    private boolean dateYn;

    @Schema(description = "유니크(고유값) 컬럼 여부", example = "false")
    private boolean uniqueYn;

    @Schema(description = "등록일시", example = "2026-08-26T09:00:00")
    private LocalDateTime createdAt;

    public static AnlColRsp from(AnlCol col) {
        return new AnlColRsp(
                col.getColumnId(),
                col.getAnlDset() != null ? col.getAnlDset().getDatasetId() : null,
                col.getColumnName(),
                col.getColumnNameKo(),
                col.getColumnNameEn(),
                col.getDataType(),
                col.getDataLength(),
                col.getDataScale(),
                col.isNumericYn(),
                col.isDateYn(),
                col.isUniqueYn(),
                col.getCreatedAt()
        );
    }
}
