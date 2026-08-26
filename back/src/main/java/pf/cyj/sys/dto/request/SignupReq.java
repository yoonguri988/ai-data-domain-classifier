package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원가입 요청 - 로그인 아이디/이메일이 중복되지 않으면 등록되고 ROLE_USER 가 기본 부여된다. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupReq {

    @Schema(description = "로그인 아이디 (4~50자)", example = "cyj0703")
    @NotBlank(message = "로그인 아이디는 필수입니다.")
    @Size(min = 4, max = 50, message = "로그인 아이디는 4~50자여야 합니다.")
    private String loginId;

    @Schema(description = "비밀번호 (영문/숫자/특수문자 포함 8자 이상)", example = "Passw0rd!23")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 영문/숫자/특수문자를 포함해 8자 이상이어야 합니다."
    )
    private String password;

    @Schema(description = "사용자 이름", example = "최윤정")
    @NotBlank(message = "사용자명은 필수입니다.")
    @Size(max = 100)
    private String userName;

    @Schema(description = "이메일", example = "cyjjeong98@gmail.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 150)
    private String email;

    @Schema(description = "휴대폰 번호 (선택)", example = "010-1234-5678")
    @Size(max = 20)
    private String phoneNo;

    @Schema(description = "소속 부서명 (선택)", example = "개발팀")
    @Size(max = 100)
    private String deptName;
}
