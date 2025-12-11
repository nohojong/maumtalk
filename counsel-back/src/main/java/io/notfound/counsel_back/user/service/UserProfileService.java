package io.notfound.counsel_back.user.service;

import io.notfound.counsel_back.user.dto.UserProfileRequestDto;
import io.notfound.counsel_back.user.dto.UserProfileResponseDto;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserProfile;
import io.notfound.counsel_back.user.repository.UserProfileRepository;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;

    // GET: 프로필 조회
    @Transactional
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserProfile profile = profileRepository.findByUser(user)
                .orElseGet(() -> {
                    // 프로필 자동 생성 + 기본값
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .gender("선택안함")
                            .age(0)
                            .interests("")
                            .concern("")
                            .build();
                    return profileRepository.save(newProfile);
                });

        return toDto(profile, user);
    }

    // PUT: 프로필 업데이트
    @Transactional
    public UserProfileResponseDto updateUserProfile(Long userId, UserProfileRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserProfile profile = profileRepository.findByUser(user)
                .orElseGet(() -> {
                    // 프로필 자동 생성 + 기본값
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .gender("선택안함")
                            .age(0)
                            .interests("")
                            .concern("")
                            .build();
                    return profileRepository.save(newProfile);
                });

        // 엔티티 값 업데이트
        profile.updateProfile(
                requestDto.getGender(),
                requestDto.getAge(),
                requestDto.getInterests(),
                requestDto.getConcern()
        );

        // JPA에서 자동 갱신되지만 안전하게 save 호출
        profileRepository.save(profile);

        return toDto(profile, user);
    }

    // DTO 변환 헬퍼
    private UserProfileResponseDto toDto(UserProfile profile, User user) {
        return UserProfileResponseDto.builder()
                .gender(profile.getGender())
                .age(profile.getAge())
                .interests(profile.getInterests())
                .concern(profile.getConcern())
                .accessUntil(
                        user.getAccessUntil() != null
                                ? user.getAccessUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                : null
                )
                .build();
    }
}
