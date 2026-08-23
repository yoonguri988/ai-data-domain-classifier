package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.type.DsetStatCd;

public record AnlDsetRsp(
        String datasetId,
        String requestNo,
        String datasetName,
        String dbSchemaName,
        String tableName,
        String dbmsTypeCode,
        DsetStatCd datasetStatus,
        Long requestedById,
        String requestedByName,
        LocalDateTime requestedAt
) {
    public static AnlDsetRsp from(AnlDset dset) {
        return new AnlDsetRsp(
                dset.getDatasetId(),
                dset.getRequestNo(),
                dset.getDatasetName(),
                dset.getDbSchemaName(),
                dset.getTableName(),
                dset.getDbmsTypeCode(),
                dset.getDatasetStatus(),
                dset.getRequestedBy() != null ? dset.getRequestedBy().getUserId() : null,
                dset.getRequestedBy() != null ? dset.getRequestedBy().getUserName() : null,
                dset.getRequestedAt()
        );
    }
}
