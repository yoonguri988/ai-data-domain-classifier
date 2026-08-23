package pf.cyj.sys.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** 데이터셋 하나에 속한 컬럼 메타데이터 일괄 등록 요청 (테이블 메타 스캔 결과 저장용) */
public record AnlColBulkCreateReq(

        @NotBlank(message = "데이터셋 ID는 필수입니다.")
        String datasetId,

        @NotEmpty(message = "등록할 컬럼이 최소 1건 이상이어야 합니다.")
        @Valid
        List<AnlColCreateReq> columns
) {
}
