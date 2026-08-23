package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;

/** 로그인 요청 */
public record LoginReq(

        @NotBlank(message = "로그인 아이디는 필수입니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
