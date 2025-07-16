package com.likelion.ai_teacher_a.global.auth.service.dto;

import org.springframework.http.HttpStatus;

import net.minidev.json.annotate.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.servlet.http.Cookie;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenRefreshResult(
	@JsonIgnore HttpStatus httpStatus,
	String message,
	String accessToken,
	long expireDateTime,
	@JsonIgnore Cookie newCookie
){}
