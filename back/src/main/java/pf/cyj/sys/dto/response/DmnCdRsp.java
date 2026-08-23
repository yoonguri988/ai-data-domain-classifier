package pf.cyj.sys.dto.response;

import pf.cyj.sys.entity.DmnCd;

public record DmnCdRsp(
        String domainCode,
        String domainNameKo,
        Integer sortOrder
) {
    public static DmnCdRsp from(DmnCd cd) {
        return new DmnCdRsp(cd.getDomainCode(), cd.getDomainNameKo(), cd.getSortOrder());
    }
}
