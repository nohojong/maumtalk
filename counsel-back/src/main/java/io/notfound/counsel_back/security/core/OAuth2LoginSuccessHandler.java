package io.notfound.counsel_back.security.core;

import io.notfound.counsel_back.common.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 시 JWT 토큰을 쿠키로 설정하고 프론트엔드로 리다이렉트하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 로그인 성공 처리 시작");

        try {
            // 1. 인증된 사용자 정보 추출
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getUser().getEmail();

            log.info("OAuth2 로그인 성공: {}, Provider: {}", email, oAuth2User.getRegistrationId());

            // 2. JWT 토큰 생성 (기존 로그인과 동일한 방식!)
            String accessToken = jwtTokenProvider.createAccessToken(
                    oAuth2User.getUser().getEmail(),
                    oAuth2User.getUser().getRole()
            );
            String refreshToken = jwtTokenProvider.createRefreshToken(
                    oAuth2User.getUser().getEmail(),
                    oAuth2User.getUser().getRole()
            );

            // 3. 쿠키에 토큰 저장 (기존 로그인과 동일한 방식!)
            CookieUtil.addAccessToken(response, accessToken);
            CookieUtil.addRefreshToken(response, refreshToken);

            // 4. 프론트엔드로 안전하게 리다이렉트 (토큰 없이!)
            response.sendRedirect("http://localhost:5173/");

            log.info("OAuth2 로그인 처리 완료: {}", email);

        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생", e);
            response.sendRedirect("http://localhost:5173/login");
        }
    }
}