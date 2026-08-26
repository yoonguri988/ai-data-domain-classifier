package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.NotiLog;

/** 알림 발송 이력(NOTIFICATION_LOG) - 수신자별 최신순 조회 */
public interface NotiLogRepository extends JpaRepository<NotiLog, Long> {

    List<NotiLog> findByTargetUser_UserIdOrderBySentAtDesc(Long userId);
}
