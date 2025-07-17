package com.likelion.ai_teacher_a.domain.userJr.dto;

import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserJrResponseDto {

    private Long id;
    private String name;
    private int schoolGrade;
    private Long parentId;
    private Long profileImageId;

    public static UserJrResponseDto from(UserJr userJr) {
        Long profileImageId = null;
        if (userJr.getProfileImage() != null && userJr.getProfileImage().getImageId() != null) {
            profileImageId = userJr.getProfileImage().getImageId();
        }

        return UserJrResponseDto.builder()
                .id(userJr.getId())
                .name(userJr.getName())
                .schoolGrade(userJr.getSchoolGrade())
                .parentId(userJr.getParent().getId())
                .profileImageId(profileImageId)
                .build();
    }
}