package com.likelion.ai_teacher_a.domain.payments.repository;

import com.likelion.ai_teacher_a.domain.payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
}
