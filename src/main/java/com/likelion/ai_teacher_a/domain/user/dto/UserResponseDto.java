package com.likelion.ai_teacher_a.domain.user.dto;

import java.time.LocalDateTime;

import com.likelion.ai_teacher_a.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {
	private Long userId;
	private String name;
	private String email;
	private String phone;
	private LocalDateTime createdAt;

	public static UserResponseDto from(User user) {
		return new UserResponseDto(
			user.getId(),
			user.getName(),
			user.getEmail(),
			user.getPhone(),
			user.getCreatedAt()
		);
	}
}