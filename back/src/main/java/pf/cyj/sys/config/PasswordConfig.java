package pf.cyj.sys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 인코더 Bean.
 * SecurityFilterChain(JWT 인증 필터, 인가 규칙, OAuth2 로그인 성공 핸들러)은 5단계(Controller)에서
 * 별도로 구성한다 — 지금은 Service 계층(회원가입/로그인)이 바로 필요로 하는 PasswordEncoder 만 먼저 등록한다.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
