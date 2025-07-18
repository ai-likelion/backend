package com.likelion.ai_teacher_a.domain.userJr.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserJrRequestDto {


    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요.")
    private String nickname;

    @NotNull
    private int schoolGrade;

}