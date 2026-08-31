package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.LoginReq;
import pf.cyj.sys.dto.request.SignupReq;
import pf.cyj.sys.dto.request.UsrRoleUpdateReq;
import pf.cyj.sys.dto.response.AppRoleRsp;
import pf.cyj.sys.dto.response.LoginRsp;
import pf.cyj.sys.dto.response.UsrAdmRsp;
import pf.cyj.sys.dto.response.UsrRsp;
import pf.cyj.sys.oauth2.CustomOAuth2User;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.security.JwtProvider;
import pf.cyj.sys.service.AuthAcntService;

/**
 * 인증/계정 - 회원가입/로그인/재발급/로그아웃, 그리고 Google/Kakao/Naver OAuth2 소셜 로그인.
 *
 * <p>이 프로젝트를 처음 보는 사람을 위한 요약:
 * <ul>
 *   <li>회원가입("/auth/signup"), 로그인("/auth/login"), 토큰 재발급("/auth/reissue")은 로그인 없이도
 *       호출 가능(SecurityConfig 에서 permitAll). 로그아웃("/auth/logout")은 Authorization 헤더만 있으면
 *       된다. 사용자 목록/권한 부여("/auth/users/**", "/auth/roles")는 그중에서도 ROLE_ADMIN 만
 *       호출 가능하다(@PreAuthorize).</li>
 *   <li>Google/Kakao/Naver 소셜 로그인은 이 Controller 에 별도 엔드포인트가 없다 - 브라우저를
 *       "/oauth2/authorization/{google|kakao|naver}" 로 이동시키면 Spring Security 가 알아서 처리하고,
 *       성공하면 {@link pf.cyj.sys.oauth2.OAuth2SuccessHandler} 가 토큰을 발급해 프론트엔드로 리다이렉트한다.
 *       (Swagger UI 에서는 리다이렉트 흐름이라 직접 테스트하기 어렵고, 브라우저 주소창에서 직접 열어야 한다.)</li>
 *   <li>Refresh Token 은 응답 JSON 에 담기지 않고 HttpOnly {@code refreshToken} 쿠키로만 내려간다(쿠키 기반
 *       흐름). Swagger UI 로 테스트하면 응답 바디에는 accessToken 만 보이고, 쿠키는 브라우저 개발자도구의
 *       Application &gt; Cookies 탭에서 확인해야 한다. 로그아웃은 같은 이름의 쿠키를 Max-Age=0 으로 내려
 *       즉시 만료시킨다.</li>
 * </ul>
 */
@Tag(name = "Auth", description = "인증/계정 - 회원가입/로그인/재발급/로그아웃 및 OAuth2 소셜 로그인")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthAcntController {

    private final AuthAcntService authAcntService;
    private final JwtProvider jwtProvider; // refreshToken 쿠키 maxAge 계산용(getRefreshTokenValiditySeconds())

    @Operation(
            summary = "회원가입",
            description = "로그인 아이디/이메일이 중복되지 않으면 새 사용자를 등록하고 ROLE_USER 를 기본 부여한다. "
                    + "성공하면 201 Created 와 함께 등록된 사용자 정보(UsrRsp)를 반환한다."
    )
    @PostMapping("/signup")
    public ResponseEntity<UsrRsp> signup(@Valid @RequestBody SignupReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authAcntService.signup(req));
    }

    @Operation(
            summary = "로그인 (Access Token 발급 + Refresh Token 쿠키 발급)",
            description = "아이디/비밀번호를 검증하고 Access Token 을 발급한다. "
                    + "Access Token 은 응답 바디(accessToken)로 내려가고, Refresh Token 은 응답 바디에는 담기지 않고 "
                    + "HttpOnly refreshToken 쿠키로만 내려간다. 이후 다른 API 호출 시에는 응답으로 받은 accessToken 을 "
                    + "Authorization: Bearer {accessToken} 헤더에 담아 호출한다(Swagger UI 우측 상단 Authorize 버튼)."
    )
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

    @Operation(
            summary = "Access Token 재발급 (Refresh Token 회전)",
            description = "요청에 자동으로 함께 담겨 오는 refreshToken 쿠키가 Redis 에 저장된 값과 일치할 때만 "
                    + "새 Access Token 을 발급하고 Refresh Token 을 회전(재발급)한다. 회전된 Refresh Token 도 "
                    + "쿠키로만 다시 내려간다. Swagger UI 에서는 브라우저가 쿠키를 자동으로 실어 보내므로 "
                    + "먼저 /auth/login 을 호출해 쿠키를 발급받은 뒤 이 API 를 호출해야 한다."
    )
    @PostMapping("/reissue")
    public ResponseEntity<LoginRsp> reissue(
            @Parameter(
                    description = "로그인 시 내려받은 HttpOnly refreshToken 쿠키. 브라우저가 자동으로 실어 보낸다 "
                            + "(Swagger UI 에서 직접 입력하는 값이 아니다).",
                    example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.xxxxx"
            )
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

    @Operation(
            summary = "(관리자) 전체 사용자 목록 조회",
            description = "가입된 모든 사용자를 현재 부여된 roles 와 함께 반환한다(UsrRsp 에는 roles 가 없어서 "
                    + "관리자 화면 전용으로 UsrAdmRsp 를 쓴다). ROLE_ADMIN 권한이 필요하다."
    )
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsrAdmRsp>> findAllUsers() {
        return ResponseEntity.ok(authAcntService.findAllUsers());
    }

    @Operation(
            summary = "(관리자) 부여 가능한 역할 목록 조회",
            description = "APP_ROLE 에 등록된 전체 역할(ROLE_ADMIN/ROLE_REVIEWER/ROLE_USER)을 반환한다. "
                    + "사용자 권한 부여 화면의 역할 선택 콤보박스에 쓴다. ROLE_ADMIN 권한이 필요하다."
    )
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppRoleRsp>> findAllRoles() {
        return ResponseEntity.ok(authAcntService.findAllRoles());
    }

    @Operation(
            summary = "(관리자) 사용자 권한 부여/회수",
            description = "지정한 사용자에게 역할을 부여(grant=true)하거나 회수(grant=false)한다. 예를 들어 "
                    + "일반 사용자로 가입한 계정을 표준 도메인 승인자(ROLE_REVIEWER)로 승격시킬 때 쓴다. "
                    + "이미 가진 역할을 다시 부여하거나 없는 역할을 회수해도 에러 없이 그대로 둔다(멱등). "
                    + "ROLE_ADMIN 권한이 필요하다."
    )
    @PatchMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsrAdmRsp> updateUserRole(
            @Parameter(description = "대상 사용자 ID", example = "1000")
            @PathVariable("userId") Long userId,
            @Valid @RequestBody UsrRoleUpdateReq req) {
        return ResponseEntity.ok(authAcntService.updateUserRole(userId, req));
    }

    @Operation(
            summary = "로그아웃",
            description = "현재 로그인한 사용자의 Refresh Token 을 Redis 에서 삭제하고 감사이력(REFRESH_TOKEN)에 "
                    + "폐기 처리한 뒤, refreshToken 쿠키도 즉시 만료시킨다. "
                    + "Authorization: Bearer {accessToken} 헤더가 필요하다(Swagger UI 상단 Authorize 버튼으로 등록)."
    )
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
