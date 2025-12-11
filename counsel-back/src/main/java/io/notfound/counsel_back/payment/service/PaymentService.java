package io.notfound.counsel_back.payment.service;

import io.notfound.counsel_back.payment.entity.Payment;
import io.notfound.counsel_back.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import io.notfound.counsel_back.user.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PaymentService {

    private final PortoneService portoneService;
    private final UserService userService;
    private final PaymentRepository paymentRepository;

    // ✅ 테스트 모드 활성화
    private final boolean TEST_MODE = true;

    public PaymentService(PortoneService portoneService, UserService userService, PaymentRepository paymentRepository) {
        this.portoneService = portoneService;
        this.userService = userService;
        this.paymentRepository = paymentRepository;
    }

    /**
     * 결제 검증 후 이용권 갱신 및 결제 기록 저장
     */
    public void verifyAndSavePayment(Long userId, Map<String, String> paymentData) throws IOException {
        boolean paid;

        if (TEST_MODE) {
            // 테스트 모드에서는 무조건 성공 처리
            paid = true;
        } else {
            // 실제 모드: 포트원 검증
            /*
            String impUid = paymentData.get("imp_uid");
            String accessToken = portoneService.getAccessToken();
            paid = portoneService.verifyPayment(impUid, accessToken);
            */
        }

        // 결제 기록 생성 및 저장
        Payment payment = Payment.builder()
                .user(userService.getUserById(userId))
                .impUid(paymentData.getOrDefault("imp_uid", "test_imp_uid"))
                .merchantUid(paymentData.getOrDefault("merchant_uid", "test_merchant_uid"))
                .amount(Integer.parseInt(paymentData.getOrDefault("amount", "10000")))
                .status(paid ? "paid" : "failed")
                .paidAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // 이용권 갱신
        if (paid) {
            LocalDateTime newExpiry = LocalDateTime.now().plusMonths(1);
            userService.updateAccessUntil(userId, newExpiry);
        } else {
            throw new IllegalStateException("결제 검증 실패");
        }
    }
}
