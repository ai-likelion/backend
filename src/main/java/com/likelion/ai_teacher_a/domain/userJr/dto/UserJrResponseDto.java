package com.likelion.ai_teacher_a.domain.userJr.dto;

import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserJrResponseDto {
    private Long userJrId;
    private String name;
    private String schoolGrade;
    private Long parentId;
    private Long profileImageId;

    public static UserJrResponseDto from(UserJr jr) {
        return new UserJrResponseDto(
                jr.getUserJrId(),
                jr.getName(),
                jr.getSchoolGrade(),
                jr.getParent() != null ? jr.getParent().getUserId() : null,
                jr.getProfileImage() != null ? jr.getProfileImage().getImageId() : null
        );
    }
}