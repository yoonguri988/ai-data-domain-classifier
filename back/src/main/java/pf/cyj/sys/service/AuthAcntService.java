package pf.cyj.sys.service;

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
import pf.cyj.sys.dto.request.TknReissueReq;
import pf.cyj.sys.dto.response.LoginRsp;
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
 * 인증/계정 - 회원가입, 로그인, Google OAuth2 로그인, Access/Refresh Token 발급·재발급, 로그아웃.
 * Refresh Token 은 TokenStore(Redis) 를 1차 저장소(TTL)로 사용하고, REFRESH_TOKEN 테이블에는
 * SHA-256 해시로 감사이력만 남긴다.
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

    @Transactional
    public UsrRsp signup(SignupReq req) {
        if (appUsrRepository.existsByLoginId(req.loginId())) {
            throw new BizRuleException("DUPLICATE_LOGIN_ID", "이미 사용 중인 로그인 아이디입니다: " + req.loginId());
        }
        if (appUsrRepository.existsByEmail(req.email())) {
            throw new BizRuleException("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다: " + req.email());
        }

        AppUsr usr = appUsrRepository.save(
                AppUsr.builder()
                        .loginId(req.loginId())
                        .password(passwordEncoder.encode(req.password()))
                        .userName(req.userName())
                        .email(req.email())
                        .phoneNo(req.phoneNo())
                        .deptName(req.deptName())
                        .build()
        );

        grantDefaultRole(usr);

        return UsrRsp.from(usr);
    }

    @Transactional
    public LoginRsp login(LoginReq req) {
        AppUsr usr = appUsrRepository.findByLoginId(req.loginId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 로그인 아이디입니다: " + req.loginId()));

        if (usr.getPassword() == null || !passwordEncoder.matches(req.password(), usr.getPassword())) {
            throw new AuthException("INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        assertLoginable(usr);

        usr.setLastLoginAt(LocalDateTime.now());

        return issueTokens(usr);
    }

    /**
     * Google OAuth2 로그인 성공 후 호출된다(OAuth2SuccessHandler).
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

    @Transactional
    public LoginRsp reissue(TknReissueReq req) {
        Long userId = jwtProvider.getUserId(req.refreshToken());

        String stored = tokenStore.getRefreshToken(userId);
        if (stored == null || !stored.equals(req.refreshToken())) {
            throw new AuthException("INVALID_REFRESH_TOKEN", "만료되었거나 유효하지 않은 Refresh Token 입니다.");
        }

        AppUsr usr = appUsrRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다: " + userId));

        revokeRefreshToken(userId, req.refreshToken());

        return issueTokens(usr);
    }

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

        tokenStore.saveRefreshToken(usr.getUserId(), refreshToken, jwtProvider.getRefreshTokenValiditySeconds());

        rfshTknRepository.save(
                RfshTkn.builder()
                        .appUsr(usr)
                        .tokenHash(sha256Hex(refreshToken))
                        .expiresAt(LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenValiditySeconds()))
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
