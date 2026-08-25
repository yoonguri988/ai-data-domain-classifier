package pf.cyj.sys.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.CmnCdGrp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CmnCdGrpRsp {

    private String codeGroup;
    private String groupName;

    public static CmnCdGrpRsp from(CmnCdGrp grp) {
        return new CmnCdGrpRsp(grp.getCodeGroup(), grp.getGroupName());
    }
}
