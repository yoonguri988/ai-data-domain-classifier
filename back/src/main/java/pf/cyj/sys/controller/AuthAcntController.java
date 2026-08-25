package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.LoginReq;
import pf.cyj.sys.dto.request.SignupReq;
import pf.cyj.sys.dto.response.LoginRsp;
import pf.cyj.sys.dto.response.UsrRsp;
import pf.cyj.sys.oauth2.CustomOAuth2User;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.security.JwtProvider;
import pf.cyj.sys.service.AuthAcntService;

/**
 * 인증/계정 - 회원가입/로그인/재발급은 SecurityConfig 에서 permitAll("/auth/signup", "/auth/login",
 * "/auth/reissue")로 열어두고, 로그아웃만 별도로 인증을 요구한다(그 외 경로는 anyRequest().authenticated()).
 * Google/Kakao/Naver OAuth2 로그인은 Spring Security 가 자체 제공하는 "/oauth2/authorization/{provider}",
 * "/login/oauth2/code/{provider}" 필터 흐름 + OAuth2SuccessHandler 로 처리되므로 이 Controller 에는
 * 별도 엔드포인트가 없다.
 * Refresh Token 은 쿠키 기반 흐름이다 - LoginRsp.getRefreshToken() 은 @JsonIgnore 라 응답 JSON 에는 안 실리고,
 * 여기서 그 값을 꺼내 ResponseCookie 로 HttpOnly refreshToken 쿠키를 내려준다(로그아웃은 같은 이름의
 * 쿠키를 Max-Age=0 으로 내려 즉시 만료시킨다).
 */
@Tag(name = "Auth", description = "인증/계정 - 회원가입/로그인/재발급/로그아웃")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthAcntController {

    private final AuthAcntService authAcntService;
    private final JwtProvider jwtProvider; // refreshToken 쿠키 maxAge 계산용(getRefreshTokenValiditySeconds())

    @Operation(summary = "회원가입", description = "로그인 아이디/이메일이 중복되지 않으면 새 사용자를 등록하고 ROLE_USER 를 기본 부여한다.")
    @PostMapping("/signup")
    public ResponseEntity<UsrRsp> signup(@Valid @RequestBody SignupReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authAcntService.signup(req));
    }

    @Operation(summary = "로그인", description = "아이디/비밀번호를 검증하고 Access Token 을 발급한다. "
            + "Refresh Token 은 응답 바디에 담기지 않고 HttpOnly refreshToken 쿠키로만 내려간다.")
    @PostMapping("/login")
    public ResponseEntity<LoginRsp> login(@Valid @RequestBody LoginReq req) {
        LoginRsp loginRsp = authAcntService.login(req);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginRsp.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtProvider.getRefreshTokenValiditySeconds())
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginRsp);
    }

    @Operation(summary = "Access Token 재발급", description = "요청에 함께 담긴 refreshToken 쿠키가 Redis 에 저장된 값과 "
            + "일치할 때만 새 Access Token 을 발급하고 Refresh Token 을 회전(재발급)한다. 회전된 Refresh Token 도 "
            + "쿠키로만 다시 내려간다.")
    @PostMapping("/reissue")
    public ResponseEntity<LoginRsp> reissue(
            @Parameter(description = "로그인 시 내려받은 HttpOnly refreshToken 쿠키. 브라우저가 자동으로 실어 보낸다.")
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        LoginRsp loginRsp = authAcntService.reissue(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginRsp.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtProvider.getRefreshTokenValiditySeconds())
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginRsp);
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 Refresh Token 을 Redis 에서 삭제하고 "
            + "감사이력(REFRESH_TOKEN)에 폐기 처리한 뒤, refreshToken 쿠키도 즉시 만료시킨다. "
            + "Authorization: Bearer {accessToken} 헤더가 필요하다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomOAuth2User principal) {
        ActorContext actor = ActorContext.from(principal);
        authAcntService.logout(actor.getUserId());

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, deleteCookie.toString()).build();
    }
}
