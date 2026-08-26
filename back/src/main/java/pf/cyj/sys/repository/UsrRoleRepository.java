package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.UsrRole;
import pf.cyj.sys.entity.UsrRoleId;

/** 사용자-역할 매핑(USER_ROLE, 복합키) - 사용자에게 부여된 역할 조회/회수 */
public interface UsrRoleRepository extends JpaRepository<UsrRole, UsrRoleId> {

    List<UsrRole> findByUserId(Long userId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
