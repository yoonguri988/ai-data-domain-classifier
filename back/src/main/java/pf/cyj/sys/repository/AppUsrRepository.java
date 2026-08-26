package pf.cyj.sys.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AppUsr;

/** 사용자 계정(APP_USER) - 로그인 아이디/이메일 조회 및 중복 체크 */
public interface AppUsrRepository extends JpaRepository<AppUsr, Long> {

    Optional<AppUsr> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);
}
