//package io.notfound.counsel_back.common.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * 프로젝트 전역에서 사용되는 공통 Bean과 설정을 등록하는 클래스
// */
//@Configuration
//public class CommonConfig {
//
//    /**
//     * ObjectMapper Bean
//     * - JSON 직렬화/역직렬화용
//     * - Controller/Service 등에서 @Autowired로 주입받아 사용
//     */
//    @Bean
//    public ObjectMapper objectMapper() {
//        return new ObjectMapper();
//    }
//
//    /**
//     * PasswordEncoder Bean
//     * - 비밀번호 암호화 및 검증용
//     * - AuthService, UserService 등에서 사용
//     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    /**
//     * WebMvcConfigurer Bean
//     * - 공통 CORS 정책 설정
//     * - 필요 시 인터셉터, 메시지 컨버터 등 추가 가능
//     */
//    @Bean
//    public WebMvcConfigurer webMvcConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOrigins("*")   // 실제 배포 시 허용할 도메인으로 변경 필요
//                        .allowedMethods("*");  // GET, POST, PUT, DELETE 등 허용
//            }
//        };
//    }
//}
