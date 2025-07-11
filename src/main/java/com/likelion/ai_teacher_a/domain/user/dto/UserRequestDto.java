package com.likelion.ai_teacher_a.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {
    private String name;
    private String email;
    private String password;
    private String phone;
}
