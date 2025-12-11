package io.notfound.counsel_back.security.core;

import io.notfound.counsel_back.security.core.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwtToken = extractTokenFromCookies(request);

//        if (StringUtils.hasText(jwtToken) && jwtTokenProvider.validateToken(jwtToken)) {
//            Authentication authentication = jwtTokenProvider.getAuthentication(jwtToken);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
        try {  // 🔹 추가: permitAll 요청에서 토큰 없거나 예외 발생 시에도 통과
            if (StringUtils.hasText(jwtToken) && jwtTokenProvider.validateToken(jwtToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(jwtToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext(); // 🔹 예외 발생 시 SecurityContext 초기화
            // 🔹 주석: permitAll 요청이면 예외 발생해도 chain.doFilter 호출되어 요청 계속 진행
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
