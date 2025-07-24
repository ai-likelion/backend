package com.likelion.ai_teacher_a.domain.payments.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentKey; // Toss에서 반환하는 결제 키

    @Column(nullable = false)
    private String orderId;

    private String method; // 결제 수단 (e.g. 카드, 가상계좌)

    private int amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String requestedAt;
    private String approvedAt;

    private String failReason;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
