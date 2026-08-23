package pf.cyj.sys.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.OauthAcnt;

public interface OauthAcntRepository extends JpaRepository<OauthAcnt, Long> {

    Optional<OauthAcnt> findByProviderAndProviderUserId(String provider, String providerUserId);
}
