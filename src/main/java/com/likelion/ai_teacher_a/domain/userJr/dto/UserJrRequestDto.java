package com.likelion.ai_teacher_a.domain.userJr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserJrRequestDto {


    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요.")
    private String nickname;

    @Min(value = 1, message = "학년은 1학년 이상이어야 합니다.")
    @Max(value = 6, message = "학년은 6학년 이하여야 합니다.")
    @NotNull
    private Integer schoolGrade;

}