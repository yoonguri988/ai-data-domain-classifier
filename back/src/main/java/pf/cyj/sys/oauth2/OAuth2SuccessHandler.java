package pf.cyj.sys.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pf.cyj.sys.dto.response.LoginRsp;
import pf.cyj.sys.security.JwtProvider;
import pf.cyj.sys.service.AuthAcntService;

/**
 * Google OAuth2 로그인 성공 시 호출된다.
 * - OAuth2User 의 표준 속성(sub/email/name)으로 로컬 계정을 연결하거나 새로 만들고 JWT 를 발급한다.
 * - Refresh Token 은 HttpOnly 쿠키로, Access Token 은 프론트엔드 리다이렉트 URL 의 쿼리 파라미터로 전달한다
 *   (일반 로그인의 LoginRsp 응답 바디 방식과 달리, 브라우저 리다이렉트라 바디를 못 쓰기 때문).
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final String PROVIDER_GOOGLE = "GOOGLE";

    private final AuthAcntService authAcntService;
    private final JwtProvider jwtProvider;

    @Value("${app.oauth2.redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        String providerUserId = String.valueOf(attrs.get("sub"));
        String email = (String) attrs.get("email");
        String name = (String) attrs.get("name");

        try {
            LoginRsp result = authAcntService.loginWithOAuth(PROVIDER_GOOGLE, providerUserId, email, name);

            Cookie refreshCookie = new Cookie("refreshToken", result.refreshToken());
            refreshCookie.setHttpOnly(true);
            boolean isLocal = "localhost".equals(request.getServerName()) || "127.0.0.1".equals(request.getServerName());
            refreshCookie.setSecure(!isLocal);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge((int) jwtProvider.getRefreshTokenValiditySeconds());
            response.addCookie(refreshCookie);

            response.sendRedirect(redirectUrl + "?accessToken=" + result.accessToken());
        } catch (Exception e) {
            response.sendRedirect(redirectUrl + "?error=true");
        }
    }
}
