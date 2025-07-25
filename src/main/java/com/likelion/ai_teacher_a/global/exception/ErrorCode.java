package com.likelion.ai_teacher_a.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {


    //사용자 관련
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    UNAUTHORIZED("인증되지 않은 사용자입니다."),
    FORBIDDEN("접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다."),
    IMAGE_NOT_FOUND("이미지를 찾을 수 없습니다."),
    USER_JR_NOT_FOUND("자녀 정보를 찾을 수 없습니다."),

    // 결제 관련
    PAYMENT_NOT_FOUND("결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_COMPLETED("이미 결제된 주문입니다."),
    PAYMENT_AMOUNT_MISMATCH("결제 금액이 일치하지 않습니다."),
    INVALID_PAYMENT_KEY("유효하지 않은 결제 키입니다."),
    TOSS_CONFIRM_FAILED("토스 결제 승인에 실패했습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
