package pf.cyj.sys.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 로그인 성공 응답 - Access Token 발급 결과.
 * refreshToken 은 응답 JSON 에는 안 실리고(@JsonIgnore) HttpOnly refreshToken 쿠키로만 내려간다
 * (Swagger UI 로 테스트할 때는 응답 바디에 accessToken 만 보이고, 쿠키는 브라우저 개발자도구의
 * Application > Cookies 탭에서 확인해야 한다). 필드 자체는 그대로 두고 직렬화만 막은 이유는
 * Controller/OAuth2SuccessHandler 가 쿠키를 설정할 때 loginRsp.getRefreshToken() 으로 바로 꺼내 쓰기
 * 위함이다 — 이 값을 위해 별도 래퍼 타입을 새로 만들 필요가 없다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRsp {

    @Schema(description = "API 호출 시 Authorization: Bearer {accessToken} 헤더에 담아 사용하는 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.xxxxx")
    private String accessToken;

    @Schema(hidden = true) // 응답 JSON 에는 안 실리고 HttpOnly 쿠키로만 내려간다
    @JsonIgnore
    private String refreshToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "Access Token 만료까지 남은 시간(초)", example = "1800")
    private long expiresIn;

    @Schema(description = "로그인한 사용자 정보")
    private UsrRsp user;

    public static LoginRsp of(String accessToken, String refreshToken, long expiresIn, UsrRsp user) {
        return new LoginRsp(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
