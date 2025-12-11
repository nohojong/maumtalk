package io.notfound.counsel_back.security.filter;

import io.notfound.counsel_back.security.jwt.JwtTokenProvider; // JWT 토큰 생성 및 검증을 담당하는 클래스
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security의 필터 체인에서 JWT 인증을 담당하는 커스텀 필터입니다.
 * 모든 HTTP 요청마다 이 필터를 거쳐 JWT 토큰의 유효성을 검사합니다.
 */
@Component // 이 클래스를 Spring 빈으로 등록하여 의존성 주입이 가능하게 합니다.
@RequiredArgsConstructor // final 필드인 jwtTokenProvider를 주입받기 위한 생성자를 자동 생성합니다.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // HTTP 요청 헤더에서 JWT를 추출할 때 사용되는 상수들
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * HTTP 요청이 들어올 때마다 실행되는 핵심 필터 메서드입니다.
     * @param request 요청 객체
     * @param response 응답 객체
     * @param filterChain 다음 필터로의 연결
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 토큰 추출
        String jwt = resolveToken(request);

        // 2. 추출된 토큰의 유효성 검사
        // 토큰이 존재하고, JwtTokenProvider를 통해 유효성이 검증되면 다음 단계를 진행합니다.
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateAccessToken(jwt)) {
            // 3. 토큰이 유효할 경우, 인증(Authentication) 객체 생성
            // 토큰에 담긴 사용자 정보를 기반으로 인증 객체를 만듭니다.
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);

            // 4. SecurityContext에 인증 정보 저장
            // SecurityContextHolder에 인증 객체를 설정하여, 해당 요청이 인증되었음을 Spring Security에 알립니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 다음 필터로 요청 전달
        // 현재 필터의 처리가 끝났으므로, 다음 필터(혹은 컨트롤러)로 요청을 넘깁니다.
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 헤더에서 "Authorization" 값을 파싱하여 JWT 토큰을 추출하는 헬퍼 메서드입니다.
     * "Bearer [토큰]" 형식에서 "Bearer " 부분을 제거하고 토큰 값만 반환합니다.
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열 또는 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        // "Bearer " 접두사가 있고, 토큰 값이 비어있지 않은지 확인합니다.
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            // "Bearer " 이후의 문자열(실제 토큰)을 반환합니다.
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
