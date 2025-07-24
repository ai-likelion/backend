package com.likelion.ai_teacher_a.domain.payments.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RefundResponseDto {
    private String paymentKey;
    private int refundedAmount;
    private String status;          // 예: "REFUNDED", "FAILED"
    private LocalDateTime refundedAt;
    private String message;         // 상세 메시지
}
