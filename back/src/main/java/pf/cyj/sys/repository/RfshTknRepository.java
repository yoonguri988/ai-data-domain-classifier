package pf.cyj.sys.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.RfshTkn;

/**
 * REFRESH_TOKEN - 실서비스 1차 저장소는 Redis(TTL)이며, 여기는 감사/추적용 백업 이력이다.
 * 폐기(revoke) 처리는 Service 계층에서 조회 후 revokedYn = true 로 저장한다.
 */
public interface RfshTknRepository extends JpaRepository<RfshTkn, Long> {

    Optional<RfshTkn> findByTokenHash(String tokenHash);

    List<RfshTkn> findByAppUsr_UserIdAndRevokedYnFalse(Long userId);
}
