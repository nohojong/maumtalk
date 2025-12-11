package io.notfound.counsel_back.security.service;

import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service // Spring Bean으로 등록
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자 이메일을 통해 DB에서 사용자 정보를 불러와 UserDetails 객체로 반환합니다.
     * Spring Security의 Authentication Provider가 인증을 수행할 때 자동으로 호출됩니다.
     *
     * @param email 사용자의 이메일 (여기서는 'username' 역할을 합니다.)
     * @return UserDetails 객체 (사용자 정보와 권한을 포함)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 경우
     */
    @Override
    @Transactional(readOnly = true) // 데이터 읽기 전용 트랜잭션으로 설정
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. 이메일을 사용하여 DB에서 사용자를 찾습니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email));

        // 2. 찾아온 User 엔티티 정보를 UserDetails 타입으로 변환합니다.
        //    (Spring Security가 요구하는 형식)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // 사용자 ID (여기서는 이메일)
                user.getPassword(), // 암호화된 비밀번호
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}