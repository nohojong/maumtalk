package io.notfound.counsel_back.user.dto;

import lombok.Getter;

@Getter
public class UserProfileRequestDto {
    private String gender;
    private Integer age;
    private String interests;
    private String concern;
}
