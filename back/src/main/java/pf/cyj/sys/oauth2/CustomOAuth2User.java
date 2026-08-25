package pf.cyj.sys.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JWT 클레임(userId/loginId/roles)으로부터 구성되는 인증 주체.
 * JwtAuthenticationFilter 가 Access Token 을 파싱해 이 객체를 만들고 SecurityContext 에 저장한다.
 */
public class CustomOAuth2User implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String loginId;
    private final List<String> roles;

    public CustomOAuth2User(Long userId, String loginId, List<String> roles) {
        this.userId = userId;
        this.loginId = loginId;
        this.roles = (roles == null || roles.isEmpty()) ? Collections.emptyList() : roles;
    }

    @Override
    public String getPassword() {
        return "N/A";
    }

    @Override
    public String getUsername() {
        return loginId != null ? loginId : String.valueOf(userId);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }
}
