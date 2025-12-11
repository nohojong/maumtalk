package io.notfound.counsel_back.security.core;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 로그인 실패 시 처리하는 핸들러
 */
@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 로그인 실패: {}", exception.getMessage(), exception);

        try {
            // 에러 메시지 URL 인코딩
            String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);

            // 프론트엔드로 리다이렉트 (포트 5173으로 통일)
            String redirectUrl = "http://localhost:5173/login?error=oauth2&message=" + encodedMessage;

            log.info("OAuth2 실패 리다이렉트: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 실패 처리 중 오류 발생", e);
            response.sendRedirect("http://localhost:5173/login?error=oauth2");
        }
    }
}