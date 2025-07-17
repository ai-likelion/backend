package com.likelion.ai_teacher_a.domain.user.service;

import com.likelion.ai_teacher_a.global.exception.CustomException;
import com.likelion.ai_teacher_a.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import com.likelion.ai_teacher_a.domain.user.dto.UserRequestDto;
import com.likelion.ai_teacher_a.domain.user.dto.UserResponseDto;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.global.auth.util.JwtUtil;

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

	public UserResponseDto getUser(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		return UserResponseDto.from(user);
	}

	public UserResponseDto updateUser(Long id, UserRequestDto dto) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		user.setName(dto.getName());
		user.setPhone(dto.getPhone());

		return UserResponseDto.from(userRepository.save(user));
	}

	public void setProfileImage(Long userId, Long imageId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Image image = imageRepository.findById(imageId)
				.orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND)); // 이미지 관련 에러코드 추가 필요

		user.setProfileImage(image);
		userRepository.save(user);
	}

	public void deleteUserById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		userRepository.delete(user);
	}

	public void deleteUserByEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		userRepository.delete(user);
	}
}

