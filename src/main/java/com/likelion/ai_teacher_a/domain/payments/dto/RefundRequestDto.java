package com.likelion.ai_teacher_a.domain.payments.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefundRequestDto {
    private String paymentKey;
    private int amount;    // 환불 금액 (부분 환불 가능 시)
    private String reason; // 환불 사유
}
