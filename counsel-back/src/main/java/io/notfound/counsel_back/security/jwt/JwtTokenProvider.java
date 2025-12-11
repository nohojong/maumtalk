package io.notfound.counsel_back.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import io.notfound.counsel_back.user.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT(Json Web Token)의 생성, 검증, 파싱을 담당하는 유틸리티 클래스입니다.
 * 보안 관련 핵심 로직이므로, Spring 빈으로 등록하여 관리합니다.
 */
@Slf4j // 로깅을 위한 Lombok 어노테이션
@Component // 이 클래스를 Spring 빈으로 등록합니다.
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidTime;
    private final long refreshTokenValidTime;
    private final UserDetailsService userDetailsService;

    /**
     * 의존성 주입을 통해 JWT 설정값을 초기화합니다.
     * @param secret application.properties에 정의된 JWT 비밀 키
     * @param accessTokenExpirationMinutes 액세스 토큰 만료 시간 (분 단위)
     * @param refreshTokenExpirationDays 리프레시 토큰 만료 시간 (일 단위)
     * @param userDetailsService Spring Security의 사용자 정보 로드 서비스
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-minutes}") int accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days}") int refreshTokenExpirationDays,
            UserDetailsService userDetailsService) {

        // 1. 비밀 키를 Base64 문자열에서 SecretKey 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // 2. 만료 시간을 밀리초 단위로 계산
        this.accessTokenValidTime = accessTokenExpirationMinutes * 60 * 1000L;
        this.refreshTokenValidTime = refreshTokenExpirationDays * 24 * 60 * 60 * 1000L;

        this.userDetailsService =  userDetailsService;
    }

    /**
     * JWT 액세스 토큰의 유효성을 검증합니다.
     * @param jwtToken 검증할 JWT 문자열
     * @return 토큰이 유효하면 true, 아니면 false 반환
     */
    public boolean validateAccessToken(String jwtToken) {
        try {
            // Jwts.parser()를 사용해 토큰을 파싱하고 서명을 검증합니다.
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtToken);
            return true;
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었을 경우
            log.info("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            // 기타 JWT 관련 예외가 발생했을 경우 (서명 불일치, 구조 오류 등)
            log.warn("Invalid access token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT 리프레시 토큰의 유효성을 검증합니다.
     * 리프레시 토큰은 만료 예외 외에도 다양한 예외를 명시적으로 처리하여 클라이언트에 정확한 에러를 전달합니다.
     * @param jwtToken 검증할 리프레시 토큰 문자열
     * @throws JwtException 유효하지 않은 경우 예외 발생
     */
    public void validateRefreshToken(String jwtToken) throws JwtException {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new JwtException("Token is empty ");
        }
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtToken);

        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expired", e); // 토큰 만료
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException e) {
            throw new JwtException("Invalid token", e); // 지원되지 않거나, 구조가 잘못되거나, 서명 오류
        }
    }

    /**
     * 액세스 토큰을 생성합니다.
     * @param subject 토큰의 주체 (예: 사용자 이메일)
     * @param roles 사용자 역할
     * @return 생성된 액세스 토큰 문자열
     */
    public String createAccessToken(String subject, UserRole roles) {
        return createToken(subject, roles, accessTokenValidTime);
    }

    /**
     * 리프레시 토큰을 생성합니다.
     * @param subject 토큰의 주체 (예: 사용자 이메일)
     * @param roles 사용자 역할
     * @return 생성된 리프레시 토큰 문자열
     */
    public String createRefreshToken(String subject, UserRole roles) {
        return createToken(subject, roles, refreshTokenValidTime);
    }

    /**
     * JWT 토큰을 실제로 생성하는 내부 메서드입니다.
     * @param subject 토큰의 주체
     * @param roles 사용자 역할
     * @param validTime 유효 시간 (밀리초)
     * @return 생성된 토큰 문자열
     */
    private String createToken(String subject, UserRole roles, long validTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validTime);

        return Jwts.builder()
                .subject(subject) // 토큰의 주체 (사용자 식별자)
                .claim("roles", roles.name()) // 사용자 역할 정보
                .issuedAt(now) // 토큰 발행 시간
                .expiration(expiration) // 토큰 만료 시간
                .signWith(secretKey) // 토큰에 서명
                .compact(); // 토큰을 문자열로 직렬화
    }

    /**
     * JWT에서 사용자 식별자(subject)를 추출합니다.
     * @param token 파싱할 토큰 문자열
     * @return 사용자 식별자 (예: 이메일) 또는 null
     */
    public String getUserId(String token) {
        try {
            // 토큰을 파싱하여 Claims(payload)를 가져오고 subject를 추출
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            // 토큰이 유효하지 않을 경우 null 반환
            return null;
        }
    }

    /**
     * 토큰으로부터 Authentication 객체를 생성합니다.
     * 이 객체는 Spring Security의 SecurityContext에 저장됩니다.
     * @param token 토큰 문자열
     * @return 인증 객체 또는 null
     */
    public Authentication getAuthentication(String token) {
        // 토큰에서 추출한 사용자 ID로 UserDetails를 로드
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserId(token));
        if (userDetails == null) return null;

        // UserDetails와 권한 정보를 바탕으로 Authentication 객체 생성
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}