package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import pf.cyj.sys.entity.RptExpLog;

public record RptExpLogRsp(
        Long exportId,
        String datasetId,
        String fileName,
        String exportedByName,
        LocalDateTime exportedAt
) {
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
