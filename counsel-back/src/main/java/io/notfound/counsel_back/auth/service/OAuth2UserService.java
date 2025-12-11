package io.notfound.counsel_back.auth.service;

import io.notfound.counsel_back.security.core.CustomOAuth2User;
import io.notfound.counsel_back.user.entity.ProviderType;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserRole;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        UserInfo userInfo = extractUserInfo(registrationId, oAuth2User);

        if (userInfo.email == null) {
            throw new OAuth2AuthenticationException("이메일 정보가 필요합니다.");
        }

        // 사용자 조회 또는 새로 생성
        User user = getOrCreateUser(userInfo, registrationId);

        return new CustomOAuth2User(user, oAuth2User.getAttributes(), registrationId);
    }

    private User getOrCreateUser(UserInfo userInfo, String registrationId) {
        ProviderType providerType = getProviderType(registrationId);

        // providerId로 먼저 조회
        if (userInfo.providerId != null) {
            return userRepository.findByProviderId(userInfo.providerId)
                    .orElseGet(() -> {
                        // 이미 일반 계정이 있으면 providerId와 providerType 업데이트
                        return userRepository.findByEmail(userInfo.email)
                                .map(existingUser -> {
                                    existingUser.updateProvider(providerType, userInfo.providerId);
                                    return existingUser;
                                })
                                .orElseGet(() -> createNewUser(userInfo, providerType));
                    });
        }

        // providerId 없으면 이메일 기준 조회
        return userRepository.findByEmail(userInfo.email)
                .map(existingUser -> {
                    existingUser.updateProvider(providerType, userInfo.providerId);
                    return existingUser;
                })
                .orElseGet(() -> createNewUser(userInfo, providerType));
    }

    private UserInfo extractUserInfo(String registrationId, OAuth2User oAuth2User) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> extractGoogleUserInfo(oAuth2User);
            case "naver" -> extractNaverUserInfo(oAuth2User);
            default -> new UserInfo(null, null, null);
        };
    }

    private UserInfo extractGoogleUserInfo(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        if (name == null || name.isBlank()) name = email.split("@")[0];
        return new UserInfo(email, name, providerId);
    }

    private UserInfo extractNaverUserInfo(OAuth2User oAuth2User) {
        Map<String, Object> response = oAuth2User.getAttribute("response");
        if (response == null) return new UserInfo(null, null, null);
        String email = (String) response.get("email");
        String name = (String) response.get("name");
        if (name == null || name.isBlank()) name = (String) response.get("nickname");
        if (name == null || name.isBlank()) name = email.split("@")[0];
        String providerId = (String) response.get("id");
        return new UserInfo(email, name, providerId);
    }

    private ProviderType getProviderType(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ProviderType.GOOGLE;
            case "naver" -> ProviderType.NAVER;
            default -> ProviderType.LOCAL;
        };
    }

    private User createNewUser(UserInfo userInfo, ProviderType providerType) {
        User newUser = User.builder()
                .email(userInfo.email)
                .userName(userInfo.name)
                .role(UserRole.USER)
                .providerId(userInfo.providerId)
                .provider(providerType)
                .build();
        return userRepository.save(newUser);
    }

    private static class UserInfo {
        final String email;
        final String name;
        final String providerId;
        UserInfo(String email, String name, String providerId) {
            this.email = email;
            this.name = name;
            this.providerId = providerId;
        }
    }
}
