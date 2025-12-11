package io.notfound.counsel_back.security.config;

import io.notfound.counsel_back.security.filter.JwtAuthenticationFilter; // 사용자 정의 JWT 인증 필터 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security의 핵심 설정 클래스입니다.
 * 애플리케이션의 전반적인 보안 규칙을 정의합니다.
 */
@Configuration
@EnableWebSecurity // 이 어노테이션이 Spring Security를 활성화합니다.
@RequiredArgsConstructor // final 필드(jwtAuthenticationFilter)에 대한 생성자를 자동 생성합니다.
public class SecurityConfig {

    // 우리가 직접 만든 JWT 인증 필터를 주입받습니다. 이 필터는 HTTP 요청이 들어올 때마다 실행됩니다.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 비밀번호를 안전하게 암호화하기 위한 BCryptPasswordEncoder 빈을 등록합니다.
     * 이 빈은 회원가입이나 로그인 시 비밀번호를 검증하는 데 사용됩니다.
     * @return PasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 과정의 핵심인 AuthenticationManager를 빈으로 노출합니다.
     * 이 객체는 사용자의 인증(로그인)을 실제로 처리하는 역할을 합니다.
     * @param authenticationConfiguration Spring Security의 인증 설정 객체
     * @return AuthenticationManager 객체
     * @throws Exception 예외
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Spring Security의 필터 체인을 설정하는 핵심 메서드입니다.
     * HTTP 요청에 대한 보안 규칙을 세밀하게 정의할 수 있습니다.
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF, 폼 로그인, HTTP Basic 인증을 비활성화합니다.
                // JWT 기반의 REST API 서버이므로 세션을 사용하지 않고, 불필요한 보안 기능을 끕니다.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 2. 세션 관리 정책을 STATELESS(상태 없음)로 설정합니다.
                // 서버가 클라이언트의 상태를 저장하지 않고, 모든 요청을 독립적으로 처리합니다.
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. HTTP 요청에 대한 인가(Authorization) 규칙을 설정합니다.
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // '/api/auth/**' 경로로 들어오는 모든 요청(회원가입, 로그인)은 인증 없이 허용합니다.
                                .requestMatchers("/api/auth/**").permitAll()
                                // 위에서 허용한 경로를 제외한 나머지 모든 요청은 반드시 인증이 필요합니다.
                                .anyRequest().authenticated()
                )

                // 4. 커스텀 필터인 JwtAuthenticationFilter를 추가합니다.
                // 이 필터는 UsernamePasswordAuthenticationFilter 이전에 실행되어,
                // 요청 헤더의 JWT를 검증하고 사용자의 인증 정보를 SecurityContext에 저장합니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
