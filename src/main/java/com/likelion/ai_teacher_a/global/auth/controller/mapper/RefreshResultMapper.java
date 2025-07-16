package com.likelion.ai_teacher_a.global.auth.controller.mapper;

import org.springframework.http.ResponseEntity;

import com.likelion.ai_teacher_a.global.auth.service.dto.TokenRefreshResult;

public class RefreshResultMapper {
	public static ResponseEntity<?> toResponseEntity(TokenRefreshResult tokenRefreshResult) {
		return ResponseEntity.status(tokenRefreshResult.httpStatus())
			.body(tokenRefreshResult);
	}
}
