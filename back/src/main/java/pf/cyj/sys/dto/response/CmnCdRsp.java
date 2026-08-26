package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.CmnCd;

/** 공통코드 상세(COMMON_CODE) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CmnCdRsp {

    @Schema(description = "코드 그룹", example = "JOB_STAT_CD")
    private String codeGroup;

    @Schema(description = "코드값", example = "RUNNING")
    private String codeValue;

    @Schema(description = "코드명", example = "실행중")
    private String codeName;

    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;

    @Schema(description = "사용 여부", example = "true")
    private boolean useYn;

    public static CmnCdRsp from(CmnCd cd) {
        return new CmnCdRsp(cd.getCodeGroup(), cd.getCodeValue(), cd.getCodeName(), cd.getSortOrder(), cd.isUseYn());
    }
}
