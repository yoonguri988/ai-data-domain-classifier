package pf.cyj.sys.dto.response;

import pf.cyj.sys.entity.CmnCd;

public record CmnCdRsp(
        String codeGroup,
        String codeValue,
        String codeName,
        Integer sortOrder,
        boolean useYn
) {
    public static CmnCdRsp from(CmnCd cd) {
        return new CmnCdRsp(cd.getCodeGroup(), cd.getCodeValue(), cd.getCodeName(), cd.getSortOrder(), cd.isUseYn());
    }
}
