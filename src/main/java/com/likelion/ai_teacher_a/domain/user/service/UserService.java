package com.likelion.ai_teacher_a.domain.user.service;

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

	public UserResponseDto createUser(UserRequestDto dto) {
		User user = new User();
		user.setName(dto.getName());
		user.setEmail(dto.getEmail());
		user.setPassword(dto.getPassword()); // ⚠️ 실제 서비스에서는 암호화 필요
		user.setPhone(dto.getPhone());

		User saved = userRepository.save(user);
		return UserResponseDto.from(saved);
	}

	public UserResponseDto getUser(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("User not found"));
		return UserResponseDto.from(user);
	}

	public UserResponseDto updateUser(Long id, UserRequestDto dto) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("User not found"));

		user.setName(dto.getName());
		user.setPhone(dto.getPhone());

		return UserResponseDto.from(userRepository.save(user));
	}

	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	// 기존 createUser, getUser 등과 함께 위치
	public void setProfileImage(Long userId, Long imageId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("User not found"));

		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new RuntimeException("Image not found"));

		user.setProfileImage(image);
		userRepository.save(user);
	}
}

