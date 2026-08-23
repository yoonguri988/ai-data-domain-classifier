package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 공통코드 그룹 등록/수정 요청 */
public record CmnCdGrpSaveReq(

        @NotBlank(message = "코드 그룹은 필수입니다.")
        @Size(max = 30)
        String codeGroup,

        @NotBlank(message = "그룹명은 필수입니다.")
        @Size(max = 100)
        String groupName
) {
}
