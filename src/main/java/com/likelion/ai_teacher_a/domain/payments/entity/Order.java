package com.likelion.ai_teacher_a.domain.payments.entity;

import com.likelion.ai_teacher_a.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Toss에 전달하는 고유 주문번호 (orderId)
    @Column(nullable = false, unique = true)
    private String orderId;

    // 상품 이름
    @Column(nullable = false)
    private String orderName;

    // 결제 금액
    @Column(nullable = false)
    private Long amount;

    // 주문 상태 (READY, PAID, FAILED 등)
    @Column(nullable = false)
    private String status;

    // 주문 생성 시각
    private LocalDateTime requestedAt;

    // 결제 완료 시각
    private LocalDateTime paidAt;

    // 결제 승인 후 Toss에서 제공하는 paymentKey
    private String paymentKey;

    // 구매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
