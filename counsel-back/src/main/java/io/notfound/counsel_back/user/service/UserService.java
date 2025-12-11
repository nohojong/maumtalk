package io.notfound.counsel_back.user.service;

import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원 조회
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * 회원 삭제 (탈퇴)
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    /**
     * 이용권 만료일 조회
     */
    @Transactional(readOnly = true)
    public LocalDateTime getAccessUntil(Long userId) {
        // 기존 getUserById 메서드를 재활용하여 사용자를 찾고 만료일을 반환
        User user = getUserById(userId);
        return user.getAccessUntil();  // 만료일 반환
    }

    /**
     * 이용권 만료일 갱신
     */
    @Transactional
    public void updateAccessUntil(Long userId, LocalDateTime newExpiry) {
        // 기존 getUserById 메서드를 재활용하여 사용자를 찾고 만료일 갱신
        User user = getUserById(userId);
        user.setAccessUntil(newExpiry);  // 만료일 갱신
        userRepository.save(user); // JPA 더티 체킹으로 인해 save() 호출은 필수는 아니지만, 명시적으로 저장하는 것을 권장
    }
}
