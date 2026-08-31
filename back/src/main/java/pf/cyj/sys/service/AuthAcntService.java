package pf.cyj.sys.service;

import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.dto.request.LoginReq;
import pf.cyj.sys.dto.request.SignupReq;
import pf.cyj.sys.dto.request.UsrRoleUpdateReq;
import pf.cyj.sys.dto.response.AppRoleRsp;
import pf.cyj.sys.dto.response.LoginRsp;
import pf.cyj.sys.dto.response.UsrAdmRsp;
import pf.cyj.sys.dto.response.UsrRsp;
import pf.cyj.sys.entity.AppRole;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.entity.OauthAcnt;
import pf.cyj.sys.entity.RfshTkn;
import pf.cyj.sys.entity.UsrRole;
import pf.cyj.sys.entity.type.UsrStatCd;
import pf.cyj.sys.exception.AuthException;
import pf.cyj.sys.exception.BizRuleException;
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.repository.AppRoleRepository;
import pf.cyj.sys.repository.AppUsrRepository;
import pf.cyj.sys.repository.OauthAcntRepository;
import pf.cyj.sys.repository.RfshTknRepository;
import pf.cyj.sys.repository.UsrRoleRepository;
import pf.cyj.sys.security.JwtProvider;
import pf.cyj.sys.security.TokenStore;

/**
 * 인증/계정 - 회원가입, 로그인, Google/Kakao/Naver OAuth2 로그인, Access/Refresh Token 발급·재발급, 로그아웃.
 * Refresh Token 은 TokenStore(Redis) 를 1차 저장소(TTL)로 사용하고, REFRESH_TOKEN 테이블에는
 * SHA-256 해시로 감사이력만 남긴다. {@code LoginRsp.refreshToken} 필드는 {@code @JsonIgnore} 라 응답
 * JSON 에는 안 실리고, Controller/OAuth2SuccessHandler 가 {@code loginRsp.getRefreshToken()} 으로 값만
 * 꺼내 HttpOnly 쿠키로 내려보낸다(쿠키 기반 Refresh Token 흐름).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthAcntService {

    private static final String DEFAULT_ROLE_CODE = "ROLE_USER";

    private final AppUsrRepository appUsrRepository;
    private final AppRoleRepository appRoleRepository;
    private final UsrRoleRepository usrRoleRepository;
    private final OauthAcntRepository oauthAcntRepository;
    private final RfshTknRepository rfshTknRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenStore tokenStore;

    /** 회원가입 - 로그인 아이디/이메일 중복 체크 후 비밀번호를 인코딩해 저장하고, ROLE_USER 를 기본 부여한다. */
    @Transactional
    public UsrRsp signup(SignupReq req) {
        if (appUsrRepository.existsByLoginId(req.getLoginId())) {
            throw new BizRuleException("DUPLICATE_LOGIN_ID", "이미 사용 중인 로그인 아이디입니다: " + req.getLoginId());
        }
        if (appUsrRepository.existsByEmail(req.getEmail())) {
            throw new BizRuleException("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다: " + req.getEmail());
        }

        AppUsr usr = appUsrRepository.save(
                AppUsr.builder()
                        .loginId(req.getLoginId())
                        .password(passwordEncoder.encode(req.getPassword()))
                        .userName(req.getUserName())
                        .email(req.getEmail())
                        .phoneNo(req.getPhoneNo())
                        .deptName(req.getDeptName())
                        .build()
        );

        grantDefaultRole(usr);

        return UsrRsp.from(usr);
    }

    /** 아이디/비밀번호 로그인 - 자격 증명과 계정 상태(ACTIVE)를 검증한 뒤 토큰을 발급한다. */
    @Transactional
    public LoginRsp login(LoginReq req) {
        AppUsr usr = appUsrRepository.findByLoginId(req.getLoginId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 로그인 아이디입니다: " + req.getLoginId()));

        if (usr.getPassword() == null || !passwordEncoder.matches(req.getPassword(), usr.getPassword())) {
            throw new AuthException("INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        assertLoginable(usr);

        usr.setLastLoginAt(LocalDateTime.now());

        return issueTokens(usr);
    }

    /**
     * Google/Kakao/Naver OAuth2 로그인 성공 후 호출된다(OAuth2SuccessHandler).
     * (provider, providerUserId) 로 연결된 계정이 있으면 그대로 로그인하고,
     * 없으면 이메일이 같은 기존 계정에 연결하거나(계정 통합) 새 계정을 만든다.
     */
    @Transactional
    public LoginRsp loginWithOAuth(String provider, String providerUserId, String email, String displayName) {
        AppUsr usr = oauthAcntRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(OauthAcnt::getAppUsr)
                .orElseGet(() -> linkOrCreateOAuthUser(provider, providerUserId, email, displayName));

        assertLoginable(usr);
        usr.setLastLoginAt(LocalDateTime.now());

        return issueTokens(usr);
    }

    /**
     * refreshToken 은 이제 요청 바디가 아니라 HttpOnly 쿠키에서 Controller 가 꺼내 그대로 넘겨준다.
     * 쿠키 자체가 없거나(로그아웃 상태) 서명/형식이 잘못된 토큰이면 401(INVALID_REFRESH_TOKEN)로 통일해서
     * 응답한다 — 예전에는 @RequestBody + @NotBlank 가 공백을 걸러줬지만, 쿠키 값은 그 검증을 거치지 않아
     * 여기서 직접 방어한다.
     */
    @Transactional
    public LoginRsp reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException("INVALID_REFRESH_TOKEN", "refreshToken 쿠키가 없습니다.");
        }

        Long userId;
        try {
            userId = jwtProvider.getUserId(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException("INVALID_REFRESH_TOKEN", "만료되었거나 유효하지 않은 Refresh Token 입니다.");
        }

        String stored = tokenStore.getRefreshToken(userId);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new AuthException("INVALID_REFRESH_TOKEN", "만료되었거나 유효하지 않은 Refresh Token 입니다.");
        }

        AppUsr usr = appUsrRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다: " + userId));

        revokeRefreshToken(userId, refreshToken);

        return issueTokens(usr);
    }

    /**
     * (관리자) 전체 사용자 목록 - 시드 데이터(admin/reviewer1) 말고도 회원가입으로 새로 들어온 사용자에게
     * 관리자가 직접 ROLE_REVIEWER 등을 부여할 수 있어야 해서 만들었다. UsrRsp 대신 roles 가 포함된
     * UsrAdmRsp 를 쓴다.
     */
    public List<UsrAdmRsp> findAllUsers() {
        return appUsrRepository.findAll().stream()
                .map(usr -> UsrAdmRsp.from(usr, resolveRoles(usr.getUserId())))
                .toList();
    }

    /** (관리자) 부여 가능한 전체 역할 목록 - 사용자 권한 부여 화면의 역할 선택 콤보박스용. */
    public List<AppRoleRsp> findAllRoles() {
        return appRoleRepository.findAll().stream().map(AppRoleRsp::from).toList();
    }

    /**
     * (관리자) 사용자 권한 부여/회수 - grant=true 면 USER_ROLE 에 (userId, roleId) 를 추가하고, false 면
     * 제거한다. 이미 가진 역할을 다시 부여하거나, 없는 역할을 회수해도 예외 없이 조용히 그대로 둔다
     * (멱등 처리 - 관리자가 화면에서 체크박스를 실수로 두 번 눌러도 에러가 나면 안 된다).
     */
    @Transactional
    public UsrAdmRsp updateUserRole(Long userId, UsrRoleUpdateReq req) {
        AppUsr usr = appUsrRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다: " + userId));
        AppRole role = appRoleRepository.findByRoleCode(req.getRoleCode())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 역할 코드입니다: " + req.getRoleCode()));

        if (Boolean.TRUE.equals(req.getGrant())) {
            usrRoleRepository.save(UsrRole.builder().userId(userId).roleId(role.getRoleId()).build());
        } else {
            usrRoleRepository.deleteByUserIdAndRoleId(userId, role.getRoleId());
        }

        return UsrAdmRsp.from(usr, resolveRoles(userId));
    }

    /** 로그아웃 - Redis 에 저장된 Refresh Token 을 삭제하고 감사이력(REFRESH_TOKEN)에 폐기 처리한다. */
    @Transactional
    public void logout(Long userId) {
        String stored = tokenStore.getRefreshToken(userId);
        tokenStore.deleteRefreshToken(userId);

        if (stored != null) {
            revokeRefreshToken(userId, stored);
        }
    }

    private void assertLoginable(AppUsr usr) {
        if (usr.getUserStatus() != UsrStatCd.ACTIVE) {
            throw new AuthException("INVALID_ACCOUNT_STATUS", "로그인할 수 없는 계정 상태입니다: " + usr.getUserStatus());
        }
    }

    private AppUsr linkOrCreateOAuthUser(String provider, String providerUserId, String email, String displayName) {
        // Google 계정 이메일이 기존 로컬 계정의 로그인 아이디와 같으면 같은 사용자로 간주해 연결한다(계정 통합).
        AppUsr usr = appUsrRepository.findByLoginId(email)
                .orElseGet(() -> appUsrRepository.save(
                        AppUsr.builder()
                                .loginId(email)
                                .userName(displayName != null ? displayName : email)
                                .email(email)
                                .build()
                ));

        oauthAcntRepository.save(
                OauthAcnt.builder().appUsr(usr).provider(provider).providerUserId(providerUserId).build()
        );

        grantDefaultRole(usr);

        return usr;
    }

    private void grantDefaultRole(AppUsr usr) {
        appRoleRepository.findByRoleCode(DEFAULT_ROLE_CODE).ifPresent(role ->
                usrRoleRepository.save(
                        UsrRole.builder().userId(usr.getUserId()).roleId(role.getRoleId()).build()
                )
        );
    }

    private List<String> resolveRoles(Long userId) {
        return usrRoleRepository.findByUserId(userId).stream()
                .map(UsrRole::getRoleId)
                .map(appRoleRepository::findById)
                .flatMap(Optional::stream)
                .map(AppRole::getRoleCode)
                .toList();
    }

    private LoginRsp issueTokens(AppUsr usr) {
        List<String> roles = resolveRoles(usr.getUserId());

        String accessToken = jwtProvider.createAccessToken(usr.getUserId(), usr.getLoginId(), roles);
        String refreshToken = jwtProvider.createRefreshToken(usr.getUserId());
        long refreshTtl = jwtProvider.getRefreshTokenValiditySeconds();

        tokenStore.saveRefreshToken(usr.getUserId(), refreshToken, refreshTtl);

        rfshTknRepository.save(
                RfshTkn.builder()
                        .appUsr(usr)
                        .tokenHash(sha256Hex(refreshToken))
                        .expiresAt(LocalDateTime.now().plusSeconds(refreshTtl))
                        .build()
        );

        return LoginRsp.of(accessToken, refreshToken, jwtProvider.getAccessTokenValiditySeconds(), UsrRsp.from(usr));
    }

    private void revokeRefreshToken(Long userId, String refreshToken) {
        rfshTknRepository.findByTokenHash(sha256Hex(refreshToken)).ifPresent(tkn -> tkn.setRevokedYn(true));
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
