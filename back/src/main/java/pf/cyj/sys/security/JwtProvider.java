package pf.cyj.sys.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JWT Access/Refresh Token 발급 및 검증 (HS256).
 * 서명/만료/발급자 검증에 실패하면 io.jsonwebtoken.JwtException 계열 예외를 그대로 던진다 —
 * JwtAuthenticationFilter 가 이를 잡아 인증 실패로 처리한다(401 응답은 SecurityConfig 의
 * authenticationEntryPoint 가 최종 결정).
 */
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties props;

    public String createAccessToken(Long userId, String loginId, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + props.getAccessTokenValiditySeconds() * 1000);

        return Jwts.builder()
                .setIssuer(props.getIssuer())
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("loginId", loginId, "roles", roles))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Refresh Token 은 재발급 시 subject(userId) 만 있으면 되므로 클레임을 추가로 싣지 않는다 */
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + props.getRefreshTokenValiditySeconds() * 1000);

        return Jwts.builder()
                .setIssuer(props.getIssuer())
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .requireIssuer(props.getIssuer())
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public long getAccessTokenValiditySeconds() {
        return props.getAccessTokenValiditySeconds();
    }

    public long getRefreshTokenValiditySeconds() {
        return props.getRefreshTokenValiditySeconds();
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
