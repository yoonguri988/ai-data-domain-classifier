package pf.cyj.sys.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AppRole;

/** 역할 마스터(APP_ROLE) - 역할 코드(ROLE_USER, ROLE_ADMIN 등)로 조회 */
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByRoleCode(String roleCode);
}
