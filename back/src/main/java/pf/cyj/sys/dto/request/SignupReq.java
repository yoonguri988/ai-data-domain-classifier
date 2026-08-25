package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원가입 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupReq {

    @NotBlank(message = "로그인 아이디는 필수입니다.")
    @Size(min = 4, max = 50, message = "로그인 아이디는 4~50자여야 합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 영문/숫자/특수문자를 포함해 8자 이상이어야 합니다."
    )
    private String password;

    @NotBlank(message = "사용자명은 필수입니다.")
    @Size(max = 100)
    private String userName;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phoneNo;

    @Size(max = 100)
    private String deptName;
}
