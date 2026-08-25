package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.type.DsetStatCd;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlDsetRsp {

    private String datasetId;
    private String requestNo;
    private String datasetName;
    private String dbSchemaName;
    private String tableName;
    private String dbmsTypeCode;
    private DsetStatCd datasetStatus;
    private Long requestedById;
    private String requestedByName;
    private LocalDateTime requestedAt;

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
