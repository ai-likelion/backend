package com.likelion.ai_teacher_a.domain.userJr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserJrUpdateRequestDto {

    private String name;

    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요.")
    private String nickname;

    @Min(value = 1, message = "학년은 최소 1이어야 합니다.")
    @Max(value = 9, message = "학년은 최대 9까지 가능합니다.")
    private Integer schoolGrade;  // 예: 중1 = 7
}
