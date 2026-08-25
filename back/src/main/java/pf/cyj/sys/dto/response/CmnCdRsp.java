package pf.cyj.sys.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.CmnCd;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CmnCdRsp {

    private String codeGroup;
    private String codeValue;
    private String codeName;
    private Integer sortOrder;
    private boolean useYn;

    public static CmnCdRsp from(CmnCd cd) {
        return new CmnCdRsp(cd.getCodeGroup(), cd.getCodeValue(), cd.getCodeName(), cd.getSortOrder(), cd.isUseYn());
    }
}
