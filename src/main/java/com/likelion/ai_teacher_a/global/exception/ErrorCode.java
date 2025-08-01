package com.likelion.ai_teacher_a.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    UNAUTHORIZED("인증되지 않은 사용자입니다."),
    FORBIDDEN("접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다."),
    IMAGE_NOT_FOUND("이미지를 찾을 수 없습니다."),
    USER_JR_NOT_FOUND("자녀 정보를 찾을 수 없습니다.");


    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
