package com.likelion.ai_teacher_a.global.auth.controller;

import static com.likelion.ai_teacher_a.global.auth.controller.mapper.RefreshResultMapper.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.ai_teacher_a.global.auth.service.AuthService;
import com.likelion.ai_teacher_a.global.auth.service.dto.TokenRefreshResult;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshAccessToken(
		@CookieValue("refresh_token") String refreshToken,
		HttpServletResponse response
	) {
		TokenRefreshResult tokenRefreshResult = authService.refreshAccessToken(refreshToken);
		response.addCookie(tokenRefreshResult.newCookie());
		return toResponseEntity(tokenRefreshResult);
	}
}
