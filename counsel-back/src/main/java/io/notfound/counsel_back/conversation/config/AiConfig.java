//package io.notfound.counsel_back.conversation.config;
//
//import org.springframework.ai.chat.memory.ChatMemoryRepository;
//import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.transaction.PlatformTransactionManager;
//
//@Configuration
//public class AiConfig {
//
//    @Bean
//    @Primary
//    public ChatMemoryRepository chatMemoryRepository(
//            JdbcTemplate jdbcTemplate,
//            PlatformTransactionManager transactionManager) {
//        return JdbcChatMemoryRepository.builder()
//                .jdbcTemplate(jdbcTemplate)
//                .transactionManager(transactionManager)
//                .build();
//    }
//}
