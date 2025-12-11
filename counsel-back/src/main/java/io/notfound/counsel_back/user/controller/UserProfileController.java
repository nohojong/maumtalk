package io.notfound.counsel_back.user.controller;

import io.notfound.counsel_back.user.dto.UserProfileRequestDto;
import io.notfound.counsel_back.user.dto.UserProfileResponseDto;
import io.notfound.counsel_back.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    // GET: 프로필 조회
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(@PathVariable Long userId) {
        UserProfileResponseDto dto = profileService.getUserProfile(userId);
        return ResponseEntity.ok(dto);
    }

    // PUT: 프로필 수정
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponseDto> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileRequestDto requestDto
    ) {
        UserProfileResponseDto dto = profileService.updateUserProfile(userId, requestDto);
        return ResponseEntity.ok(dto);
    }
}
