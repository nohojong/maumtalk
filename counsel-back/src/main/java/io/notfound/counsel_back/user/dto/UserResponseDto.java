package io.notfound.counsel_back.user.dto;

import io.notfound.counsel_back.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {

    private Long id;
    private String email;
    private LocalDateTime accessUntil; // 결제 종료일

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.accessUntil = user.getAccessUntil();
    }
}
