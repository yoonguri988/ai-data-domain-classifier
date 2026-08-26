package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.CmnCdGrp;

/** 공통코드 그룹(COMMON_CODE_GROUP) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CmnCdGrpRsp {

    @Schema(description = "코드 그룹 (PK)", example = "JOB_STAT_CD")
    private String codeGroup;

    @Schema(description = "코드 그룹명", example = "배치작업 상태")
    private String groupName;

    public static CmnCdGrpRsp from(CmnCdGrp grp) {
        return new CmnCdGrpRsp(grp.getCodeGroup(), grp.getGroupName());
    }
}
