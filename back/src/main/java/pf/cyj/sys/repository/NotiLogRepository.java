package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.NotiLog;

public interface NotiLogRepository extends JpaRepository<NotiLog, Long> {

    List<NotiLog> findByTargetUser_UserIdOrderBySentAtDesc(Long userId);
}
