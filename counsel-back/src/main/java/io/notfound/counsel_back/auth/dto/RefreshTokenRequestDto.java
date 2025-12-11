package io.notfound.counsel_back.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequestDto {
    private String refreshToken;
}