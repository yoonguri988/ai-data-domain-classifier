package pf.cyj.sys.oauth2;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pf.cyj.sys.dto.response.LoginRsp;
import pf.cyj.sys.security.JwtProvider;
import pf.cyj.sys.service.AuthAcntService;

/**
 * Google/Kakao/Naver OAuth2 로그인 성공 핸들러.
 *
 * <p>동작 흐름(처음 보는 사람을 위한 요약):
 * <ol>
 *   <li>사용자가 "/oauth2/authorization/{google|kakao|naver}" 로 이동하면 Spring Security 가
 *       해당 공급자의 로그인 화면으로 리다이렉트한다.</li>
 *   <li>사용자가 로그인/동의를 마치면 공급자가 "/login/oauth2/code/{provider}" 로 콜백을 보내고,
 *       Spring Security 가 이 콜백을 처리해 인증에 성공하면 이 핸들러(onAuthenticationSuccess)가 호출된다.</li>
 *   <li>공급자마다 사용자 정보(attributes)의 모양이 달라서 {@link UserInfoOAuth2} 구현체
 *       ({@link UserInfoGoogle}/{@link UserInfoKakao}/{@link UserInfoNaver})로 공통 형태로 변환한다.</li>
 *   <li>계정 연결/생성 + Access/Refresh Token 발급 + Refresh Token 저장(Redis/감사이력)은 전부
 *       {@link AuthAcntService#loginWithOAuth} 가 담당한다 - 로컬 아이디/비밀번호 로그인과 완전히 같은
 *       토큰 발급 경로를 재사용해서 두 흐름의 토큰 정책(만료시간, roles 클레임 등)이 어긋나지 않게 한다.</li>
 *   <li>Refresh Token 은 HttpOnly refreshToken 쿠키로 내려주고(AuthAcntController 의 로그인/재발급과
 *       동일한 방식), Access Token 은 프론트엔드 리다이렉트 URL 뒤에 쿼리스트링(?accessToken=...)으로
 *       실어 보낸다 - 리다이렉트라서 응답 바디를 못 쓰기 때문이다.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthAcntService authAcntService;
    private final JwtProvider jwtProvider; // refreshToken 쿠키 maxAge 계산용(getRefreshTokenValiditySeconds())

    /** 로그인 성공 후 Access Token 을 쿼리스트링으로 실어 돌려보낼 프론트엔드 주소 (application-oauth.yml) */
    @Value("${app.oauth2.redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        // 공급자 식별(google, kakao, naver) - application-oauth.yml 의 registration id 와 동일하다.
        String registrationId = ((OAuth2AuthenticationToken) authentication)
                .getAuthorizedClientRegistrationId();

        // 공급자별 원시 attributes 를 공통 인터페이스(UserInfoOAuth2)로 매핑
        UserInfoOAuth2 userInfo = switch (registrationId) {
            case "google" -> new UserInfoGoogle(attrs);
            case "kakao" -> new UserInfoKakao(attrs);
            case "naver" -> new UserInfoNaver(attrs);
            default -> throw new IllegalArgumentException("지원하지 않는 Provider: " + registrationId);
        };

        // (provider, providerId) 로 계정 연결/생성 + Access/Refresh Token 발급 (로컬 로그인과 동일 경로)
        LoginRsp loginRsp = authAcntService.loginWithOAuth(
                userInfo.getProvider(), userInfo.getProviderId(), userInfo.getEmail(), userInfo.getNickname());

        // refreshToken 을 쿠키로 설정 (AuthAcntController 의 login/reissue 와 동일한 옵션)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginRsp.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtProvider.getRefreshTokenValiditySeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // redirectUrl(리액트 경로)로 accessToken 전달
        String targetUrl = redirectUrl + "?accessToken=" + loginRsp.getAccessToken();
        response.sendRedirect(targetUrl);
    }
}
