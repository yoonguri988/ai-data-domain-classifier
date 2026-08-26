package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 데이터셋 하나에 속한 컬럼 메타데이터를 한 번에 여러 건 등록하는 요청 (테이블 메타 스캔 결과 저장용) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnlColBulkCreateReq {

    @Schema(description = "컬럼을 등록할 데이터셋 ID (AnlDsetCreateReq 로 등록 후 응답에서 받은 값)", example = "DS_00000001")
    @NotBlank(message = "데이터셋 ID는 필수입니다.")
    private String datasetId;

    @Schema(description = "등록할 컬럼 메타 목록 (최소 1건)")
    @NotEmpty(message = "등록할 컬럼이 최소 1건 이상이어야 합니다.")
    @Valid
    private List<AnlColCreateReq> columns;
}
