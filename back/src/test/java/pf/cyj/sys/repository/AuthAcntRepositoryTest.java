package pf.cyj.sys.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.entity.AppRole;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.OauthAcnt;
import pf.cyj.sys.entity.RfshTkn;
import pf.cyj.sys.entity.UsrRole;
import pf.cyj.sys.entity.type.UsrStatCd;

/**
 * 인증/계정 그룹 Repository 테스트 - AppUsr, AppRole, UsrRole, OauthAcnt, RfshTkn.
 * 테스트마다 @Transactional 로 자동 롤백되므로 실제 개발용 Oracle 18c XE 스키마에 데이터가 남지 않는다.
 */
@SpringBootTest
@Transactional
class AuthAcntRepositoryTest {

    @Autowired
    AppUsrRepository appUsrRepository;
    @Autowired
    AppRoleRepository appRoleRepository;
    @Autowired
    UsrRoleRepository usrRoleRepository;
    @Autowired
    OauthAcntRepository oauthAcntRepository;
    @Autowired
    RfshTknRepository rfshTknRepository;

    private AppUsr savedUsr;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();
        AppUsr usr = AppUsr.builder()
                .loginId("test-usr-" + ts)
                .password("{bcrypt}dummy-hash")
                .userName("테스트사용자")
                .email("test-usr-" + ts + "@example.com")
                .phoneNo("010-1234-5678")
                .deptName("개발팀")
                .build();

        savedUsr = appUsrRepository.save(usr);
        assertThat(savedUsr.getUserId()).isNotNull();
    }

    @Test
    @DisplayName("APP_USER 저장 후 loginId로 단건 조회")
    void testFindByLoginId() {
        Optional<AppUsr> found = appUsrRepository.findByLoginId(savedUsr.getLoginId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(savedUsr.getUserId());
        assertThat(found.get().getUserStatus()).isEqualTo(UsrStatCd.ACTIVE);
    }

    @Test
    @DisplayName("loginId/email 중복 여부 확인")
    void testExistsByLoginIdAndEmail() {
        assertThat(appUsrRepository.existsByLoginId(savedUsr.getLoginId())).isTrue();
        assertThat(appUsrRepository.existsByEmail(savedUsr.getEmail())).isTrue();
        assertThat(appUsrRepository.existsByLoginId("no-such-login-id")).isFalse();
    }

    @Test
    @DisplayName("APP_ROLE 등록 및 roleCode로 조회")
    void testAppRoleFindByRoleCode() {
        AppRole role = appRoleRepository.save(
                AppRole.builder()
                        .roleCode("ROLE_TEST_" + System.currentTimeMillis())
                        .roleName("테스트 권한")
                        .build()
        );

        Optional<AppRole> found = appRoleRepository.findByRoleCode(role.getRoleCode());

        assertThat(found).isPresent();
        assertThat(found.get().getRoleId()).isEqualTo(role.getRoleId());
    }

    @Test
    @DisplayName("USER_ROLE 매핑 등록/조회/삭제 (복합키)")
    void testUsrRoleGrantAndRevoke() {
        AppRole role = appRoleRepository.save(
                AppRole.builder()
                        .roleCode("ROLE_TEST_" + System.currentTimeMillis())
                        .roleName("테스트 권한")
                        .build()
        );

        usrRoleRepository.save(
                UsrRole.builder()
                        .userId(savedUsr.getUserId())
                        .roleId(role.getRoleId())
                        .build()
        );

        List<UsrRole> granted = usrRoleRepository.findByUserId(savedUsr.getUserId());
        assertThat(granted).extracting(UsrRole::getRoleId).contains(role.getRoleId());

        usrRoleRepository.deleteByUserIdAndRoleId(savedUsr.getUserId(), role.getRoleId());

        List<UsrRole> afterDelete = usrRoleRepository.findByUserId(savedUsr.getUserId());
        assertThat(afterDelete).extracting(UsrRole::getRoleId).doesNotContain(role.getRoleId());
    }

    @Test
    @DisplayName("OAUTH_ACCOUNT 연동 등록 및 provider+providerUserId 조회")
    void testOauthAcntFind() {
        OauthAcnt saved = oauthAcntRepository.save(
                OauthAcnt.builder()
                        .appUsr(savedUsr)
                        .provider("GOOGLE")
                        .providerUserId("google-uid-" + System.currentTimeMillis())
                        .build()
        );

        Optional<OauthAcnt> found =
                oauthAcntRepository.findByProviderAndProviderUserId("GOOGLE", saved.getProviderUserId());

        assertThat(found).isPresent();
        assertThat(found.get().getAppUsr().getUserId()).isEqualTo(savedUsr.getUserId());
    }

    @Test
    @DisplayName("REFRESH_TOKEN 발급 이력 저장 및 tokenHash/유효토큰 조회")
    void testRfshTknFind() {
        RfshTkn saved = rfshTknRepository.save(
                RfshTkn.builder()
                        .appUsr(savedUsr)
                        .tokenHash("hash-" + System.currentTimeMillis())
                        .expiresAt(LocalDateTime.now().plusDays(14))
                        .build()
        );

        Optional<RfshTkn> found = rfshTknRepository.findByTokenHash(saved.getTokenHash());
        assertThat(found).isPresent();
        assertThat(found.get().isRevokedYn()).isFalse();

        List<RfshTkn> active = rfshTknRepository.findByAppUsr_UserIdAndRevokedYnFalse(savedUsr.getUserId());
        assertThat(active).extracting(RfshTkn::getTokenId).contains(saved.getTokenId());
    }
}
