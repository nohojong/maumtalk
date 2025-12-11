package io.notfound.counsel_back.security.core;

import io.notfound.counsel_back.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 일반 로그인(UserDetails)과 OAuth 로그인(OAuth2User) 정보를 모두 담는 통합 DTO 클래스.
 * Spring Security의 인증 정보(Principal)로 사용됩니다.
 */
@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    // 일반 로그인 사용자를 위한 생성자
    public CustomUserDetails(User user) {
        this(user, null);
    }

    // OAuth 로그인 사용자를 위한 생성자
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // UserDetails 메서드 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // OAuth 사용자는 null, 일반 사용자는 비밀번호
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    // OAuth2User 메서드 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getEmail(); // 고유한 식별자를 반환
    }

    // 편의 메서드
    public Long getUserId() {
        return user.getId();
    }

    public String getUserName() {
        return user.getUserName();
    }
}