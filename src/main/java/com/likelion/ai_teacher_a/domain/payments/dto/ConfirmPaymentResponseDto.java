package com.likelion.ai_teacher_a.domain.payments.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConfirmPaymentResponseDto {
    private String paymentKey;
    private String orderId;
    private int amount;
    private String method;
    private String status;
    private String approvedAt;
    private String requestedAt;
    private String failReason;
}

