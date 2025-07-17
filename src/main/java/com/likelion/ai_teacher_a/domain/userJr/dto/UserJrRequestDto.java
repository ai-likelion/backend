package com.likelion.ai_teacher_a.domain.userJr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserJrRequestDto {

    @NotBlank
    private String name;

    @NotNull
    private Integer schoolGrade;

    @NotNull
    private Long parentId;
}