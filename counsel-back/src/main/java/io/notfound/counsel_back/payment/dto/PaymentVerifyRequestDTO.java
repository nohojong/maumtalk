package io.notfound.counsel_back.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentVerifyRequestDTO {
    private String impUid;
    private String merchantUid;
    private int amount; // 선택사항, 검증용
}
