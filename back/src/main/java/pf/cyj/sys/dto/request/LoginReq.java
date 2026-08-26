package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 로그인 요청 - 아이디/비밀번호로 Access Token + Refresh Token 을 발급받는다. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginReq {

    @Schema(description = "로그인 아이디", example = "cyj0703")
    @NotBlank(message = "로그인 아이디는 필수입니다.")
    private String loginId;

    @Schema(description = "비밀번호", example = "Passw0rd!23")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
