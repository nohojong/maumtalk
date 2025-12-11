package io.notfound.counsel_back.auth.controller;

import io.notfound.counsel_back.auth.dto.LoginRequestDto;
import io.notfound.counsel_back.auth.dto.UserInfoResponse;
import io.notfound.counsel_back.auth.service.AuthService;
import io.notfound.counsel_back.common.exception.CustomException;
import io.notfound.counsel_back.common.exception.ErrorCode;
import io.notfound.counsel_back.common.response.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage<Void>> signup(@Valid @RequestBody LoginRequestDto request) {
        authService.register(request);
        return ResponseEntity.ok(ResponseMessage.of(200, "회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseMessage<Void>> login(@Valid @RequestBody LoginRequestDto request,
                                                       HttpServletResponse response) {
        authService.login(request, response);
        return ResponseEntity.ok(ResponseMessage.of(200, "로그인이 완료되었습니다."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseMessage<Void>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        authService.refreshAccessToken(refreshToken, response);
        return ResponseEntity.ok(ResponseMessage.of(200, "토큰이 갱신되었습니다."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseMessage<Void>> logout(HttpServletRequest request,
                                                        HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(ResponseMessage.of(200, "로그아웃이 완료되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseMessage<UserInfoResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        UserInfoResponse userInfo = authService.getUserInfo(email);

        return ResponseEntity.ok(ResponseMessage.of(200, "사용자 정보 조회 성공", userInfo));
    }
}