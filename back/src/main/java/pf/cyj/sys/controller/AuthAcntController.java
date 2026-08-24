package pf.cyj.sys.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import pf.cyj.sys.dto.request.LoginReq;
import pf.cyj.sys.dto.request.SignupReq;
import pf.cyj.sys.dto.request.TknReissueReq;
import pf.cyj.sys.dto.response.LoginRsp;
import pf.cyj.sys.dto.response.UsrRsp;
import pf.cyj.sys.oauth2.CustomUserPrincipal;
import pf.cyj.sys.security.ActorContext;
import pf.cyj.sys.service.AuthAcntService;

/**
 * 인증/계정 - 회원가입/로그인/재발급은 SecurityConfig 에서 permitAll("/auth/signup", "/auth/login",
 * "/auth/reissue")로 열어두고, 로그아웃만 별도로 인증을 요구한다(그 외 경로는 anyRequest().authenticated()).
 * Google OAuth2 로그인은 Spring Security 가 자체 제공하는 "/oauth2/authorization/google",
 * "/login/oauth2/code/google" 필터 흐름 + OAuth2SuccessHandler 로 처리되므로 이 Controller 에는
 * 별도 엔드포인트가 없다.
 */
@Tag(name="Auth", description = "인증/계정 - 회원가입/로그인/재발급용")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthAcntController {

    private final AuthAcntService authAcntService;

    @PostMapping("/signup")
    public ResponseEntity<UsrRsp> signup(@Valid @RequestBody SignupReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authAcntService.signup(req));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRsp> login(@Valid @RequestBody LoginReq req) {
        return ResponseEntity.ok(authAcntService.login(req));
    }

    @PostMapping("/reissue")
    public ResponseEntity<LoginRsp> reissue(@Valid @RequestBody TknReissueReq req) {
        return ResponseEntity.ok(authAcntService.reissue(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserPrincipal principal) {
        ActorContext actor = ActorContext.from(principal);
        authAcntService.logout(actor.userId());
        return ResponseEntity.noContent().build();
    }
}
