package io.notfound.counsel_back.security.controller;

import io.notfound.counsel_back.security.dto.LoginRequestDto;
import io.notfound.counsel_back.security.dto.LoginResponseDto;
import io.notfound.counsel_back.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증(Authentication) 관련 API를 처리하는 컨트롤러입니다.
 * 회원가입, 로그인, 로그아웃 기능을 제공합니다.
 */
@Slf4j // 로깅을 위한 Lombok 어노테이션
@RestController // RESTful 웹 서비스 컨트롤러임을 나타냅니다.
@RequestMapping("/api/auth") // 이 컨트롤러의 모든 요청은 /api/auth 경로로 시작합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동 생성하여 의존성을 주입합니다.
public class AuthController {
    private final AuthService authService; // 인증 관련 비즈니스 로직을 처리하는 서비스 클래스

    /**
     * 회원가입 API
     * @param request 회원가입에 필요한 이메일과 비밀번호 정보를 담은 DTO
     * @return 성공 시 201 Created 상태 코드와 메시지를 반환합니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody LoginRequestDto request) {
        authService.signup(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인 API
     * @param request 로그인을 위한 이메일과 비밀번호 정보를 담은 DTO
     * @return 로그인 성공 시 액세스 토큰과 리프레시 토큰이 포함된 DTO를 반환합니다.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃 API
     * 클라이언트가 토큰을 삭제하도록 유도하는 엔드포인트입니다.
     * @return 성공 시 200 OK 상태 코드와 메시지를 반환합니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // 서버 측에서 특별한 로직은 없으며, 클라이언트가 토큰을 제거하는 방식입니다.
        return ResponseEntity.ok("로그아웃이 완료되었습니다. 클라이언트에서 토큰을 삭제해주세요.");
    }
}