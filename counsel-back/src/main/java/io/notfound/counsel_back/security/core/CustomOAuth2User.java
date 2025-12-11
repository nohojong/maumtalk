package io.notfound.counsel_back.security.core;

import io.notfound.counsel_back.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * OAuth2User 인터페이스를 구현하여, 우리 서비스의 사용자 정보(User)를 담는 DTO 클래스입니다.
 * Spring Security가 인증 완료 후 사용자 정보를 CustomOAuth2User 객체로 관리하게 됩니다.
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;
    private final String registrationId;

    public CustomOAuth2User(User user, Map<String, Object> attributes, String registrationId) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        this.attributes = attributes;
        this.registrationId = registrationId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getName() {
        // OAuth2User의 nameAttributeKey와 매칭될 이름을 반환합니다.
        // providerId를 직접 사용하는 것이 더 안전합니다.
        if (user.getProviderId() != null) {
            return user.getProviderId();
        }

        // 만약 providerId가 null이라면 attributes에서 직접 가져옵니다.
        switch (registrationId.toLowerCase()) {
            case "google":
                return (String) attributes.get("sub");
            case "naver":
                Map<String, Object> naverAttributes = (Map<String, Object>) attributes.get("response");
                if (naverAttributes != null) {
                    return (String) naverAttributes.get("id");
                }
                return user.getEmail();
            default:
                return user.getEmail();
        }
    }

    /**
     * 사용자 ID를 반환합니다.
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 사용자 이메일을 반환합니다.
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * 사용자 이름을 반환합니다.
     */
    public String getUserName() {
        return user.getUserName();
    }
}