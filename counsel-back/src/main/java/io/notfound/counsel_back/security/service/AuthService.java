package io.notfound.counsel_back.security.service;

import io.notfound.counsel_back.security.dto.LoginRequestDto;
import io.notfound.counsel_back.security.dto.LoginResponseDto;
import io.notfound.counsel_back.security.dto.RefreshTokenRequestDto;
import io.notfound.counsel_back.security.jwt.JwtTokenProvider;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserRole;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증(Authentication)과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 회원가입, 로그인, 토큰 갱신 기능을 담당합니다.
 */
@Slf4j // 로깅을 위한 Lombok 어노테이션
@Service // 이 클래스를 서비스 레이어의 Spring 빈으로 등록합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동 생성하여 의존성을 주입합니다.
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    /**
     * 새로운 사용자를 등록하는 회원가입 메서드입니다.
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return 저장된 User 엔티티
     */
    @Transactional // 메서드 실행 중 예외 발생 시 롤백을 보장합니다.
    public User signup(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password)) // 비밀번호를 안전하게 해시합니다.
                .role(UserRole.USER)
                .build();
        return userRepository.save(user);
    }

    /**
     * 사용자의 로그인 정보를 검증하고 JWT 토큰을 발급합니다.
     * @param loginRequestDto 로그인 요청 DTO (이메일, 비밀번호)
     * @return 발급된 액세스 토큰과 리프레시 토큰이 포함된 DTO
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        // 1. 사용자로부터 받은 이메일과 비밀번호로 인증용 토큰 객체를 생성합니다.
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        // 2. AuthenticationManager를 통해 인증을 시도합니다.
        // 이 과정에서 CustomUserDetailsService가 호출되어 사용자 정보와 비밀번호를 검증합니다.
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);

        // 3. 인증이 성공하면, JWT 토큰을 생성합니다.
        String accessToken = jwtTokenProvider.createAccessToken(authentication.getName(), UserRole.USER);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication.getName(), UserRole.USER);

        // 4. 생성된 토큰들을 DTO에 담아 반환합니다.
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 유효한 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
     * @param refreshTokenRequestDto 리프레시 토큰이 포함된 DTO
     * @return 새로 발급된 액세스 토큰 문자열
     */
    @Transactional(readOnly = true) // 데이터 변경이 없으므로 읽기 전용으로 설정합니다.
    public String refreshAccessToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        String refreshToken = refreshTokenRequestDto.getRefreshToken();

        // 1. 리프레시 토큰의 유효성을 검증합니다.
        jwtTokenProvider.validateRefreshToken(refreshToken);

        // 2. 리프레시 토큰에서 사용자 ID(이메일)를 추출합니다.
        String userId = jwtTokenProvider.getUserId(refreshToken);
        if (userId == null) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // 3. 추출한 사용자 ID로 DB에서 사용자 정보를 조회합니다.
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 4. 조회한 사용자 정보로 새로운 액세스 토큰을 생성하여 반환합니다.
        return jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole());
    }
}