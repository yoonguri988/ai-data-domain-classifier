package pf.cyj.sys.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.OauthAcnt;

/** 소셜 로그인 연동 계정(OAUTH_ACCOUNT) - (공급자, 공급자측 사용자 ID)로 연결된 내부 계정 조회 */
public interface OauthAcntRepository extends JpaRepository<OauthAcnt, Long> {

    Optional<OauthAcnt> findByProviderAndProviderUserId(String provider, String providerUserId);
}
