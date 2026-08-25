package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AnlCol;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlColRsp {

    private Long columnId;
    private String datasetId;
    private String columnName;
    private String columnNameKo;
    private String columnNameEn;
    private String dataType;
    private Integer dataLength;
    private Integer dataScale;
    private boolean numericYn;
    private boolean dateYn;
    private boolean uniqueYn;
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
