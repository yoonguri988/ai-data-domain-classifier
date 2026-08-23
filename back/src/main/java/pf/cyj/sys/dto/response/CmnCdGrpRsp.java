package pf.cyj.sys.dto.response;

import pf.cyj.sys.entity.CmnCdGrp;

public record CmnCdGrpRsp(
        String codeGroup,
        String groupName
) {
    public static CmnCdGrpRsp from(CmnCdGrp grp) {
        return new CmnCdGrpRsp(grp.getCodeGroup(), grp.getGroupName());
    }
}
