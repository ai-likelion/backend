package com.likelion.ai_teacher_a.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.ai_teacher_a.domain.user.service.UserService;
import com.likelion.ai_teacher_a.global.auth.resolver.annotation.LoginUserId;
import com.likelion.ai_teacher_a.global.auth.util.dto.CookieResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "User Controller", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "회원 탈퇴")
	@DeleteMapping
	public ResponseEntity<Void> deleteCurrentUser(@LoginUserId Long loginId,
		@CookieValue("refresh_token") String refreshToken) {

		userService.deleteUserById(loginId, refreshToken);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@LoginUserId Long userId, @CookieValue("refresh_token") String refreshToken,
		HttpServletResponse resp) {
		CookieResponse result = userService.deleteRefreshToken(userId, refreshToken);
		resp.addCookie(result.cookie());
		return ResponseEntity.ok().build();
	}

}
