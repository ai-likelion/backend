package com.likelion.ai_teacher_a.domain.payments.controller;

import com.likelion.ai_teacher_a.domain.payments.dto.PaymentRequestDto;
import com.likelion.ai_teacher_a.domain.payments.dto.PaymentResponseDto;
import com.likelion.ai_teacher_a.domain.payments.dto.RefundRequestDto;
import com.likelion.ai_teacher_a.domain.payments.dto.RefundResponseDto;
import com.likelion.ai_teacher_a.domain.payments.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "결제", description = "결제 관련 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 승인")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponseDto> confirmPayment(
            @RequestBody PaymentRequestDto requestDto) {
        PaymentResponseDto responseDto = paymentService.confirmPayment(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "환불 요청")
    @PostMapping("/refund")
    public ResponseEntity<RefundResponseDto> refundPayment(@RequestBody RefundRequestDto refundRequestDto) {
        RefundResponseDto responseDto = paymentService.refundPayment(refundRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}
