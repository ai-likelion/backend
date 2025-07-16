package com.likelion.ai_teacher_a.global.auth.controller.mapper;

import org.springframework.http.ResponseEntity;

import com.likelion.ai_teacher_a.global.auth.controller.dto.TokenResponse;
import com.likelion.ai_teacher_a.global.auth.service.dto.TokenRefreshResult;

public class RefreshResultMapper {
	public static ResponseEntity<?> toResponseEntity(TokenRefreshResult tokenRefreshResult) {
		return ResponseEntity.status(tokenRefreshResult.httpStatus())
			.body(TokenResponse.builder()
				.accessToken(tokenRefreshResult.accessToken())
				.expireDateTime(tokenRefreshResult.expireDateTime())
				.message(tokenRefreshResult.message())
				.build());
	}
}
