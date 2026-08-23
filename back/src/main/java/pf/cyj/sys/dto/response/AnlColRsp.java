package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import pf.cyj.sys.entity.AnlCol;

public record AnlColRsp(
        Long columnId,
        String datasetId,
        String columnName,
        String columnNameKo,
        String columnNameEn,
        String dataType,
        Integer dataLength,
        Integer dataScale,
        boolean numericYn,
        boolean dateYn,
        boolean uniqueYn,
        LocalDateTime createdAt
) {
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
