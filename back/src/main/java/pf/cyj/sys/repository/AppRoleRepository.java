package pf.cyj.sys.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AppRole;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByRoleCode(String roleCode);
}
