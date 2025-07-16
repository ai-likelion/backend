package com.likelion.ai_teacher_a.global.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.ai_teacher_a.domain.user.dto.UserResponseDto;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.global.auth.resolver.annotation.LoginUserId;

import lombok.RequiredArgsConstructor;

//TODO: 추후 삭제해야하는 클래스 테스트 목적 클래스 임.
@RestController
@RequestMapping("/test/api/temp/user")
@RequiredArgsConstructor
public class TempUserController {

	private final UserRepository userRepository;

	@GetMapping("/me")
	public ResponseEntity<UserResponseDto> getLoggedInUserInfo(@LoginUserId Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
		return ResponseEntity.ok(UserResponseDto.from(user));
	}
}