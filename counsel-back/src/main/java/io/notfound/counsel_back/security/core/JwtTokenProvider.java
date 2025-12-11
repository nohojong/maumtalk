package io.notfound.counsel_back.security.core;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.notfound.counsel_back.user.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT(Json Web Token)의 생성, 검증, 파싱을 담당하는 유틸리티 클래스입니다.
 * 보안 관련 핵심 로직이므로, Spring 빈으로 등록하여 관리합니다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidTime;
    private final long refreshTokenValidTime;
    private final UserDetailsService userDetailsService;

    /**
     * 의존성 주입을 통해 JWT 설정값을 초기화합니다.
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-minutes}") int accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days}") int refreshTokenExpirationDays,
            UserDetailsService userDetailsService) {

        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenValidTime = (long) accessTokenExpirationMinutes * 60 * 1000L;
        this.refreshTokenValidTime = (long) refreshTokenExpirationDays * 24 * 60 * 60 * 1000L;
        this.userDetailsService = userDetailsService;
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     */
    public boolean validateToken(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            log.warn("Token is empty or null.");
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            log.warn("Invalid token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is invalid: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 액세스 토큰을 생성합니다.
     */
    public String createAccessToken(String subject, UserRole roles) {
        return buildToken(subject, roles, accessTokenValidTime);
    }

    /**
     * 리프레시 토큰을 생성합니다.
     */
    public String createRefreshToken(String subject, UserRole roles) {
        return buildToken(subject, roles, refreshTokenValidTime);
    }

    /**
     * JWT 토큰을 실제로 생성하는 내부 메서드입니다.
     */
    private String buildToken(String subject, UserRole roles, long validTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validTime);

        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT에서 사용자 식별자(subject)를 추출합니다.
     */
    public String getUserId(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 토큰으로부터 Authentication 객체를 생성합니다.
     */
    public Authentication getAuthentication(String token) {
        String userId = getUserId(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * 토큰 만료 시간 반환 (블랙리스트 TTL 설정용)
     * - 최신 API로 통일
     */
    public long getTokenExpiration(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration().getTime();
    }

    /**
     * 만료된 토큰도 파싱 (로그아웃 시 사용)
     * - 최신 API로 통일
     */
    public boolean validateTokenIgnoreExpiration(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return true; // 만료되어도 유효한 토큰으로 간주
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
    }
}