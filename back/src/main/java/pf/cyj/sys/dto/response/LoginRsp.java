package pf.cyj.sys.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 로그인 성공 응답 - Access Token 발급 결과.
 * refreshToken 은 응답 JSON 에는 안 실리고(@JsonIgnore) HttpOnly 쿠키로만 내려간다(쿠키 기반 Refresh
 * Token 흐름). 필드 자체는 그대로 두고 직렬화만 막은 이유는 Controller/OAuth2SuccessHandler 가 쿠키를
 * 설정할 때 loginRsp.getRefreshToken() 으로 바로 꺼내 쓰기 위함이다 — 이 값을 위해 별도 래퍼 타입을
 * 새로 만들 필요가 없다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRsp {

    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UsrRsp user;

    public static LoginRsp of(String accessToken, String refreshToken, long expiresIn, UsrRsp user) {
        return new LoginRsp(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
