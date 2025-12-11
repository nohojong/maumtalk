package io.notfound.counsel_back.user.controller;

import io.notfound.counsel_back.user.dto.UserResponseDto;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 특정 유저 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserResponseDto responseDto = new UserResponseDto(user);
        return ResponseEntity.ok(responseDto);
    }

    /** 유저 삭제 (회원 탈퇴) */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
