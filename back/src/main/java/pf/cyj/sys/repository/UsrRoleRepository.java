package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.UsrRole;
import pf.cyj.sys.entity.UsrRoleId;

public interface UsrRoleRepository extends JpaRepository<UsrRole, UsrRoleId> {

    List<UsrRole> findByUserId(Long userId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
