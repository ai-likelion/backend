package com.likelion.ai_teacher_a.domain.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponseDto {
    private String orderId;
    private String paymentKey;
    private int amount;
    private String status;
}
