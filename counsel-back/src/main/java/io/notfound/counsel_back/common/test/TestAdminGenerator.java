package io.notfound.counsel_back.common.test;

import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserRole;
import io.notfound.counsel_back.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestAdminGenerator implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestAdminGenerator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String email = "net.codecraft@gmail.com";
        if (userRepository.findByEmail(email).isPresent()) {
            return; // 이미 있으면 생성 안함
        }

        User admin = User.builder()
                .email(email)
                .userName("admin")
                .password(passwordEncoder.encode("abc123"))
                .role(UserRole.ADMIN)
                .provider(null)
                .providerId(null)
                .build();

        userRepository.save(admin);
        System.out.println("==> 임시 관리자 계정 생성됨: " + email);
    }
}
