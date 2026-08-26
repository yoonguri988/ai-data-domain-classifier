package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.type.DsetStatCd;

/** 분석대상 데이터셋(ANALYSIS_DATASET) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlDsetRsp {

    @Schema(description = "데이터셋 ID (업무키, BizIdGenerator 채번)", example = "DS0000000001")
    private String datasetId;

    @Schema(description = "요청번호 (업무키, BizIdGenerator 채번)", example = "RQ_00000001")
    private String requestNo;

    @Schema(description = "데이터셋명", example = "고객 마스터 테이블")
    private String datasetName;

    @Schema(description = "분석 대상 DB 스키마명", example = "SALES")
    private String dbSchemaName;

    @Schema(description = "분석 대상 테이블명", example = "TB_CUSTOMER")
    private String tableName;

    @Schema(description = "DBMS 종류 코드", example = "ORACLE")
    private String dbmsTypeCode;

    @Schema(description = "데이터셋 상태 - REGISTERED(등록됨)/ANALYZING(분석중)/ANALYZED(분석완료)", example = "REGISTERED")
    private DsetStatCd datasetStatus;

    @Schema(description = "등록 요청자 사용자 ID", example = "1")
    private Long requestedById;

    @Schema(description = "등록 요청자 이름", example = "최윤정")
    private String requestedByName;

    @Schema(description = "등록 요청 일시", example = "2026-08-26T09:00:00")
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
