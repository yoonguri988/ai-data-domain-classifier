package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 공통코드 상세 등록/수정 요청 */
public record CmnCdSaveReq(

        @NotBlank(message = "코드 그룹은 필수입니다.")
        @Size(max = 30)
        String codeGroup,

        @NotBlank(message = "코드값은 필수입니다.")
        @Size(max = 30)
        String codeValue,

        @NotBlank(message = "코드명은 필수입니다.")
        @Size(max = 100)
        String codeName,

        @NotNull
        Integer sortOrder,

        @NotNull
        Boolean useYn
) {
}
