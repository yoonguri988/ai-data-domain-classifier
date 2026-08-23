package pf.cyj.sys.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AppUsr;

public interface AppUsrRepository extends JpaRepository<AppUsr, Long> {

    Optional<AppUsr> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);
}
