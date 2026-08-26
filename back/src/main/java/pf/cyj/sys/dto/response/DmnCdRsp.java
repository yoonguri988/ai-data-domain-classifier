package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.DmnCd;

/** 표준 도메인 후보 마스터(DOMAIN_CODE) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DmnCdRsp {

    @Schema(description = "표준 도메인 코드", example = "DOM_PHONE")
    private String domainCode;

    @Schema(description = "표준 도메인 한글명", example = "전화번호")
    private String domainNameKo;

    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;

    public static DmnCdRsp from(DmnCd cd) {
        return new DmnCdRsp(cd.getDomainCode(), cd.getDomainNameKo(), cd.getSortOrder());
    }
}
