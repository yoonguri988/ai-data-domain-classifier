package pf.cyj.sys.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import pf.cyj.sys.oauth2.CustomOAuth2User;
/**
 * JWT 인증 사용자 정보 서비스
 * - Authentication 에서 CustomOAuth2User 에서 꺼내서 현재 로그인한 사용자 정보를 제공
 */
@Component
public class AuthUserJwtService {  
    public Long getCurrentUserId(Authentication authentication) {
        CustomOAuth2User userPrincipal = (CustomOAuth2User) authentication.getPrincipal();
        return userPrincipal.getUserId();
    }
 
    public String getCurrentUserEmail(Authentication authentication) {
        CustomOAuth2User userPrincipal = (CustomOAuth2User) authentication.getPrincipal();
        return userPrincipal.getUsername();
    }
}