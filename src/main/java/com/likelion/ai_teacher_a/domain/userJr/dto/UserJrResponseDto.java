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
    private String profileImageUrl;

    public static UserJrResponseDto from(UserJr userJr) {
        String profileImageUrl = null;
        if (userJr.getImage() != null && userJr.getImage().getUrl() != null) {
            profileImageUrl = userJr.getImage().getUrl();
        }

        return UserJrResponseDto.builder()
                .id(userJr.getUserJrId())
                .name(userJr.getNickname())
                .schoolGrade(userJr.getSchoolGrade())
                .parentId(userJr.getUser().getId())
                .profileImageUrl(profileImageUrl)
                .build();
    }


}