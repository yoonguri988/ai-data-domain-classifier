package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.RptExpLog;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RptExpLogRsp {

    private Long exportId;
    private String datasetId;
    private String fileName;
    private String exportedByName;
    private LocalDateTime exportedAt;

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
