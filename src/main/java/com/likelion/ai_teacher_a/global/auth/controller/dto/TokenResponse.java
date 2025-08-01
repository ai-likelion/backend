package com.likelion.ai_teacher_a.global.auth.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenResponse(
	String message,
	String accessToken,
	long expireDateTime
) {
}