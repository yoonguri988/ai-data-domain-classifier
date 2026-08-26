package pf.cyj.sys.security;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.oauth2.CustomOAuth2User;

/**
 * Controller 가 SecurityContext(JWT 인증 결과)에서 꺼낸 로그인 사용자 정보를 Service 계층에
 * 전달하기 위한 값 객체. Service 는 Spring Security(Authentication)에 직접 의존하지 않고
 * 이 객체만 받아서 처리한다 — 지금까지 Service 메서드들이 받던 raw Long requesterId 자리에
 * Controller 단계(5단계)부터 이 객체의 getUserId() 를 넘겨주면 된다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActorContext {

    private Long userId;
    private String loginId;
    private List<String> roles;

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    public boolean hasRole(String roleCode) {
        return roles != null && roles.contains(roleCode);
    }

    public boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }

    public static ActorContext from(CustomOAuth2User principal) {
        return new ActorContext(principal.getUserId(), principal.getUsername(), principal.getRoles());
    }
}
