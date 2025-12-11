package io.notfound.counsel_back.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProfileResponseDto {
    private String gender;
    private Integer age;
    private String interests;
    private String concern;
    private String  accessUntil; // 읽기 전용
}
