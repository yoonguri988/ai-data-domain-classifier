package pf.cyj.sys.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * jwt.* 설정값(application.yml)을 타입 안전하게 바인딩한다.
 * @Data 대신 @Getter/@Setter 만 쓰는 이유: Lombok @Data 는 toString() 을 자동 생성하는데,
 * secret 필드가 로그에 찍히는 사고를 막기 위해 toString() 을 일부러 만들지 않는다.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String issuer;
    private String secret;
    private long accessTokenValiditySeconds;
    private long refreshTokenValiditySeconds;
}
