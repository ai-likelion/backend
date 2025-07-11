package com.likelion.ai_teacher_a.domain.user.dto;

import com.likelion.ai_teacher_a.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private Long profileImageId; // 프로필 이미지 ID

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getProfileImage() != null ? user.getProfileImage().getImageId() : null
        );
    }
}

