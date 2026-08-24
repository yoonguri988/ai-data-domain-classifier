package pf.cyj.sys.config;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pf.cyj.sys.oauth2.OAuth2SuccessHandler;
import pf.cyj.sys.security.JwtAuthenticationFilter;
import pf.cyj.sys.security.JwtProvider;

/**
 * Spring Security 설정 - JWT 기반 stateless 인증 + Google OAuth2 로그인.
 * PasswordEncoder Bean 은 PasswordConfig 에 그대로 둔다(회원가입/로그인 Service 가 이미 사용 중이라
 * 굳이 옮기지 않았다).
 * authorizeHttpRequests 는 5단계(Controller)에서 확정된 실제 @RequestMapping 기준으로 다듬었다:
 * - 회원가입/로그인/재발급("/auth/signup", "/auth/login", "/auth/reissue")과 Google OAuth2 로그인
 *   흐름("/oauth2/**", "/login/**")만 명시적으로 permitAll 이고, "/auth/logout" 을 포함한 그 나머지는
 *   전부 anyRequest().authenticated() 기본값을 따른다(화이트리스트 방식 — 새 엔드포인트를 추가해도
 *   실수로 인증 없이 열리지 않는다).
 * - GET "/api/common-codes/**", "/api/domains/**" 만 예외적으로 비로그인 조회를 허용한다(화면 초기
 *   로딩용 콤보박스).
 * - 관리자 전용(공통코드 등록, 표준도메인 승인/반려/대기목록)은 여기서 경로로 막지 않고 각 Controller
 *   메서드에 @PreAuthorize("hasRole('ADMIN')") 로 세밀하게 제어한다(@EnableMethodSecurity).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // JWT 전용 stateless API 이므로 anonymous 인증을 비활성화한다.
                // (비활성화하지 않으면 인증 안 된 요청도 항상 익명 Authentication 이 채워져서
                //  authenticationEntryPoint(401)가 아니라 accessDeniedHandler(403)로만 응답이 떨어진다.
                //  프론트가 401을 보고 refreshToken 재발급을 시도하는 흐름이므로 반드시 401이 나와야 한다.)
                .anonymous(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/signup", "/auth/login", "/auth/reissue",
                                "/oauth2/**", "/login/**",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**"
                        ).permitAll()
                        // 공통코드/도메인 후보 마스터는 화면 초기 로딩용이라 비로그인 조회를 열어둔다
                        .requestMatchers(HttpMethod.GET, "/api/common-codes/**", "/api/domains/**").permitAll()
                        // "/auth/logout" 을 포함한 그 외 전부는 인증이 필요하다(화이트리스트 방식)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // 인증 자체가 안 된 경우(토큰 없음/만료/무효) → 401 JSON
                        // 프론트 axios 인터셉터가 이 401을 보고 refreshToken 으로 accessToken 재발급을 시도한다.
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"errorCode\":\"UNAUTHORIZED\",\"error\":\"인증이 필요합니다.\"}");
                        })
                        // 인증은 됐지만 권한이 부족한 경우 → 403 JSON
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"errorCode\":\"FORBIDDEN\",\"error\":\"접근 권한이 없습니다.\"}");
                        })
                )
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2SuccessHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // React 개발 서버
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
