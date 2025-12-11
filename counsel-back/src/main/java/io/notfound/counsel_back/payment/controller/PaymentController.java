package io.notfound.counsel_back.payment.controller;

import io.notfound.counsel_back.payment.service.PaymentService;
import io.notfound.counsel_back.security.core.CustomUserDetails;
import io.notfound.counsel_back.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final UserService userService;
    private final PaymentService paymentService;

    public PaymentController(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    // 사용자의 이용권 상태 조회
    @GetMapping("/access-status")
    public ResponseEntity<Map<String, Object>> getAccessStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();

        Map<String, Object> response = new HashMap<>();
        response.put("accessUntil", userService.getAccessUntil(userId));
        return ResponseEntity.ok(response);
    }

    // 결제 검증 및 이용권 갱신
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> paymentData
    ) {
        Long userId = userDetails.getUserId();

        try {
            // 테스트 모드에서는 imp_uid 없이도 처리 가능
            paymentService.verifyAndSavePayment(userId, paymentData);
            return ResponseEntity.ok("결제 완료 및 이용권 갱신 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("결제는 성공했지만 이용권 갱신 실패: " + e.getMessage());
        }
    }
}
