package io.notfound.counsel_back.security.core;

import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자 이메일을 통해 DB에서 사용자 정보를 불러와 CustomUserDetails 객체로 반환합니다.
     * 일반 로그인과 OAuth 로그인 사용자 모두 이 메서드를 통해 인증 정보를 가져옵니다.
     *
     * @param email 사용자의 이메일 (JWT 토큰의 subject)
     * @return CustomUserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. 이메일을 사용하여 DB에서 사용자를 찾습니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email));

        // 2. 찾아온 User 엔티티 정보를 CustomUserDetails 타입으로 변환하여 반환합니다.
        //    CustomUserDetails 내에서 비밀번호 필드(null)를 안전하게 처리합니다.
        return new CustomUserDetails(user);
    }
}