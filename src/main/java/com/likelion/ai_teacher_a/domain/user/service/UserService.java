package com.likelion.ai_teacher_a.domain.user.service;

import org.springframework.stereotype.Service;

import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.global.auth.util.JwtUtil;
import com.likelion.ai_teacher_a.global.exception.CustomException;
import com.likelion.ai_teacher_a.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final ImageRepository imageRepository;
	private final JwtUtil jwtUtil;

	public void deleteUserById(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		userRepository.delete(user);
	}
}

