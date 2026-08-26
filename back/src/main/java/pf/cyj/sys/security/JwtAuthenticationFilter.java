package pf.cyj.sys.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import pf.cyj.sys.oauth2.CustomOAuth2User;

/**
 * JWT 인증 필터.
 * - Authorization 헤더에서 Bearer 토큰을 꺼내 JwtProvider 로 파싱한다.
 * - 성공하면 CustomUserPrincipal 을 만들어 SecurityContext 에 저장한다.
 * - 토큰이 없거나 만료/무효해도 여기서 응답을 끝내지 않는다. 그냥 인증 안 된 상태로 다음 필터로
 *   넘기고, 최종적으로 401/403 여부는 SecurityConfig 의 authenticationEntryPoint/accessDeniedHandler 가
 *   결정한다 (그래야 /auth/** 같은 permitAll 경로가 막히지 않는다).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtProvider.parseClaims(token);

                Long userId = Long.valueOf(claims.getSubject());
                String loginId = claims.get("loginId", String.class);
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                CustomOAuth2User principal = new CustomOAuth2User(userId, loginId, roles);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                logger.debug("[JwtAuthenticationFilter] SecurityContext 에 인증 정보 저장 완료 (userId=" + userId + ")");
            } catch (ExpiredJwtException e) {
                // 정상적으로 발생할 수 있는 상황(Access Token 만료) → 스택트레이스 없이 debug 로그만
                logger.debug("[JwtAuthenticationFilter] Access Token 만료: " + e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (JwtException | IllegalArgumentException e) {
                // 서명 위조, 형식 오류 등 실제 이상 케이스만 warn 으로 남긴다
                logger.warn("[JwtAuthenticationFilter] 유효하지 않은 토큰: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("[JwtAuthenticationFilter] Authorization 헤더가 없거나 Bearer 형식이 아닙니다.");
        }

        chain.doFilter(request, response);
    }
}
