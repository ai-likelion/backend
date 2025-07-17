package com.likelion.ai_teacher_a.domain.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.ai_teacher_a.domain.user.dto.UserRequestDto;
import com.likelion.ai_teacher_a.domain.user.dto.UserResponseDto;
import com.likelion.ai_teacher_a.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "User Controller", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "사용자 id 조회")
	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> get(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getUser(id));
	}

	@Operation(summary = "사용자 정보 수정")
	@PatchMapping("/{id}")
	public ResponseEntity<UserResponseDto> update(
		@PathVariable Long id,
		@RequestBody UserRequestDto dto
	) {
		return ResponseEntity.ok(userService.updateUser(id, dto));
	}

	@Operation(summary = "사용자 프로필 이미지")
	@PatchMapping("/{userId}/profile-image")
	public ResponseEntity<Void> setProfileImage(
		@PathVariable Long userId,
		@RequestParam Long imageId) {
		userService.setProfileImage(userId, imageId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		userService.deleteUserById(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/me")
	public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
		String email = authentication.getName();
		userService.deleteUserByEmail(email);
		return ResponseEntity.noContent().build();
	}

}
