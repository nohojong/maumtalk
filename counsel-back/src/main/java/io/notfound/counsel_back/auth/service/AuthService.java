package io.notfound.counsel_back.auth.service;

import io.notfound.counsel_back.auth.dto.UserInfoResponse;
import io.notfound.counsel_back.common.exception.CustomException;
import io.notfound.counsel_back.common.exception.ErrorCode;
import io.notfound.counsel_back.common.util.CookieUtil;
import io.notfound.counsel_back.auth.dto.LoginRequestDto;
import io.notfound.counsel_back.conversation.entity.Conversation;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserRole;
import io.notfound.counsel_back.user.repository.UserRepository;
import io.notfound.counsel_back.security.core.JwtTokenProvider;
// import io.notfound.counsel_back.auth.service.TokenBlacklistService; // Redis 서비스
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    // private final TokenBlacklistService tokenBlacklistService; // Redis 구현 후 활성화

    @Transactional
    public void register(LoginRequestDto request) {
        log.info("회원가입 시도: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("이미 존재하는 이메일로 회원가입 시도: {}", request.getEmail());
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
        log.info("회원가입 완료: {}", request.getEmail());
    }

    @Transactional
    public void login(LoginRequestDto request, HttpServletResponse response) {
        log.info("로그인 시도: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 이메일로 로그인 시도: {}", request.getEmail());
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("잘못된 비밀번호로 로그인 시도: {}", request.getEmail());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRole());

        CookieUtil.addAccessToken(response, accessToken);
        CookieUtil.addRefreshToken(response, refreshToken);

        log.info("로그인 성공: {}", request.getEmail());
    }

    @Transactional
    public void refreshAccessToken(String refreshToken, HttpServletResponse response) {
        log.info("토큰 갱신 요청");

        // 1. JWT 기본 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("유효하지 않은 refresh token으로 갱신 시도");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 2. 블랙리스트 확인 (Redis 구현 후 활성화)
        /*
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            log.warn("블랙리스트된 refresh token으로 갱신 시도");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        */

        String email = jwtTokenProvider.getUserId(refreshToken);
        log.info("토큰 갱신 대상 사용자: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("토큰은 유효하지만 사용자를 찾을 수 없음: {}", email);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole());

        CookieUtil.addAccessToken(response, newAccessToken);
        log.info("토큰 갱신 완료: {}", email);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("로그아웃 요청");

        // Redis 블랙리스트 구현 후 활성화
        /*
        try {
            // 현재 사용자의 토큰들을 블랙리스트에 추가
            String accessToken = CookieUtil.getAccessToken(request);
            String refreshToken = CookieUtil.getRefreshToken(request);

            if (accessToken != null && jwtTokenProvider.validateTokenIgnoreExpiration(accessToken)) {
                tokenBlacklistService.addToBlacklist(accessToken);
            }

            if (refreshToken != null && jwtTokenProvider.validateTokenIgnoreExpiration(refreshToken)) {
                tokenBlacklistService.addToBlacklist(refreshToken);
            }

        } catch (Exception e) {
            log.warn("로그아웃 시 토큰 블랙리스트 처리 중 오류 발생", e);
            // 블랙리스트 실패해도 로그아웃은 진행
        }
        */

        CookieUtil.deleteTokens(response);
        log.info("로그아웃 완료");
    }

    public UserInfoResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + email
                ));
        return UserInfoResponse.from(user);       
    }
}