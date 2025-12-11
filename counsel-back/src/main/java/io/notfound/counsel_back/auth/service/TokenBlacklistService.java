//package io.notfound.counsel_back.auth.service;
//
//import io.notfound.counsel_back.common.exception.CustomException;
//import io.notfound.counsel_back.common.exception.ErrorCode;
//import io.notfound.counsel_back.security.core.JwtTokenProvider;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class TokenBlacklistService {
//
//    private final RedisTemplate<String, String> redisTemplate;
//    private final JwtTokenProvider jwtTokenProvider;
//
//    private static final String BLACKLIST_PREFIX = "blacklist:";
//
//    /**
//     * 토큰을 블랙리스트에 추가
//     */
//    public void addToBlacklist(String token) {
//        try {
//            String key = BLACKLIST_PREFIX + token;
//            long expiration = jwtTokenProvider.getTokenExpiration(token);
//
//            // 토큰 만료 시간까지만 Redis에 저장
//            Duration duration = Duration.ofMillis(expiration - System.currentTimeMillis());
//            if (duration.isPositive()) {
//                redisTemplate.opsForValue().set(key, "blacklisted", duration);
//                log.info("토큰이 블랙리스트에 추가됨");
//            }
//        } catch (Exception e) {
//            log.error("블랙리스트 추가 중 오류 발생", e);
//            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * 토큰이 블랙리스트에 있는지 확인
//     */
//    public boolean isBlacklisted(String token) {
//        try {
//            String key = BLACKLIST_PREFIX + token;
//            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
//        } catch (Exception e) {
//            log.error("블랙리스트 확인 중 오류 발생", e);
//            // Redis 장애 시 안전을 위해 true 반환 (토큰 무효화)
//            return true;
//        }
//    }
//}