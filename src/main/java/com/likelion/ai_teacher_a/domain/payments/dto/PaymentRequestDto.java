package com.likelion.ai_teacher_a.domain.payments.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    private String paymentKey;
    private String orderId;
    private int amount;
}
