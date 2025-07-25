package com.likelion.ai_teacher_a.domain.payments.service;

import com.likelion.ai_teacher_a.domain.payments.dto.*;
import com.likelion.ai_teacher_a.domain.payments.entity.Order;
import com.likelion.ai_teacher_a.domain.payments.entity.OrderStatus;
import com.likelion.ai_teacher_a.domain.payments.entity.Payment;
import com.likelion.ai_teacher_a.domain.payments.entity.PaymentStatus;
import com.likelion.ai_teacher_a.domain.payments.repository.OrderRepository;
import com.likelion.ai_teacher_a.domain.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final OrderRepository orderRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${toss.payments.secret-key}")
    private String tossSecretKey;

    //Toss Payments 결제 승인 처리
    public PaymentResponseDto confirmPayment(PaymentRequestDto requestDto) {
        // 필수 정보 검증
        if (requestDto.getPaymentKey() == null || requestDto.getOrderId() == null) {
            throw new IllegalArgumentException("필수 결제 정보가 누락되었습니다.");
        }
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }

        // HTTP 헤더 설정: Basic Auth 자동 처리
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(tossSecretKey, "");  // 비밀번호는 빈 문자열
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PaymentRequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            // Toss Payments 결제 승인 API 호출
            ResponseEntity<ConfirmPaymentResponseDto> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    entity,
                    ConfirmPaymentResponseDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ConfirmPaymentResponseDto body = response.getBody();

                // 금액, 주문번호 검증
                if (!body.getOrderId().equals(requestDto.getOrderId()) || body.getAmount() != requestDto.getAmount()) {
                    throw new IllegalStateException("결제 정보가 일치하지 않습니다.");
                }

                // 주문 조회
                Order order = orderRepository.findByOrderId(body.getOrderId())
                        .orElseThrow(() -> new IllegalStateException("해당 주문을 찾을 수 없습니다."));

                // 결제 성공 시 주문 상태 변경
                order.setStatus(OrderStatus.PAID.name());
                order.setPaidAt(LocalDateTime.now());
                order.setPaymentKey(body.getPaymentKey());
                orderRepository.save(order);

                Payment payment = Payment.builder()
                        .paymentKey(requestDto.getPaymentKey())
                        .orderId(requestDto.getOrderId())
                        .amount(requestDto.getAmount())
                        .method(body.getMethod())
                        .status(PaymentStatus.SUCCESS)
                        .approvedAt(body.getApprovedAt())
                        .build();

                paymentRepository.save(payment);

                return PaymentResponseDto.builder()
                        .orderId(requestDto.getOrderId())
                        .paymentKey(requestDto.getPaymentKey())
                        .amount(requestDto.getAmount())
                        .status("DONE") // 실제 API 응답값에 따라 변경
                        .build();
            } else {
                throw new IllegalStateException("결제 승인 실패: " + response.getBody());
            }
        } catch (HttpClientErrorException e) {
            throw new IllegalStateException("결제 승인 중 오류 발생: " + e.getResponseBodyAsString(), e);
        }
    }

    public RefundResponseDto refundPayment(RefundRequestDto refundRequest) {
        // 1. 환불 대상 결제 조회
        Payment payment = paymentRepository.findByPaymentKey(refundRequest.getPaymentKey())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 건입니다."));

        // 2. 환불 가능 여부 검증 (예: 이미 환불되었는지, 환불 금액 초과 여부 등)
        if (payment.getStatus() == PaymentStatus.SUCCESS && refundRequest.getAmount() > 0
                && refundRequest.getAmount() <= payment.getAmount()) {

            // 3. Toss 환불 API 호출 준비
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(tossSecretKey, "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Toss 환불 요청 DTO 작성 (필요하면 별도 클래스로 분리 가능)
            var refundPayload = new java.util.HashMap<String, Object>();
            refundPayload.put("cancelReason", refundRequest.getReason());
            refundPayload.put("cancelAmount", refundRequest.getAmount());

            HttpEntity<?> entity = new HttpEntity<>(refundPayload, headers);

            String refundUrl = "https://api.tosspayments.com/v1/payments/" + refundRequest.getPaymentKey() + "/cancel";

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(refundUrl, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    // 4. 환불 성공 처리
                    payment.setStatus(PaymentStatus.FAIL);  // 혹은 REFUNDED 상태 추가하면 그걸로 변경
                    paymentRepository.save(payment);

                    // 연관 주문 상태도 변경 가능 (예: 취소로 변경)
                    Order order = orderRepository.findByOrderId(payment.getOrderId())
                            .orElseThrow(() -> new IllegalStateException("해당 주문을 찾을 수 없습니다."));
                    order.setStatus(OrderStatus.CANCELLED.name());
                    orderRepository.save(order);

                    return RefundResponseDto.builder()
                            .paymentKey(payment.getPaymentKey())
                            .refundedAmount(refundRequest.getAmount())
                            .status("REFUNDED")
                            .refundedAt(LocalDateTime.now())
                            .message("환불이 정상 처리되었습니다.")
                            .build();
                } else {
                    throw new IllegalStateException("환불 요청 실패: " + response.getBody());
                }
            } catch (Exception e) {
                throw new IllegalStateException("환불 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException("환불 조건이 맞지 않습니다.");
        }
    }

    public void confirmPayment(String orderId) {
        log.info("결제 확인 시작: orderId={}", orderId);

        try {
            // 결제 처리 로직
            log.debug("결제 처리 중간 단계 정보 로그");

        } catch (Exception e) {
            log.error("결제 확인 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
