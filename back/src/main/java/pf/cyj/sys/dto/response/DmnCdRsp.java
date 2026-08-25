package pf.cyj.sys.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.DmnCd;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DmnCdRsp {

    private String domainCode;
    private String domainNameKo;
    private Integer sortOrder;

    public static DmnCdRsp from(DmnCd cd) {
        return new DmnCdRsp(cd.getDomainCode(), cd.getDomainNameKo(), cd.getSortOrder());
    }
}
