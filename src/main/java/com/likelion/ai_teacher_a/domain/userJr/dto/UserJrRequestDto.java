package com.likelion.ai_teacher_a.domain.userJr.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJrRequestDto {
    private Long parentId;       // 학부모 ID
    private String name;
    private String schoolGrade;
}

