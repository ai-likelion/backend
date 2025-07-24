package com.likelion.ai_teacher_a.domain.payments.entity;

public enum OrderStatus {
    READY,      // 주문 생성됨
    PAID,       // 결제 완료됨
    CANCELLED   // 주문 취소됨
}

