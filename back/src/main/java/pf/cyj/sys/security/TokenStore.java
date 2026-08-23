package pf.cyj.sys.security;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Refresh Token 1차 저장소(Redis). Key 규칙: refresh:{userId}, TTL = Refresh Token 만료시간 */
@Component
@RequiredArgsConstructor
public class TokenStore {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(Long userId, String token, long ttlSeconds) {
        redisTemplate.opsForValue().set(key(userId), token, Duration.ofSeconds(ttlSeconds));
    }

    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(key(userId));
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
