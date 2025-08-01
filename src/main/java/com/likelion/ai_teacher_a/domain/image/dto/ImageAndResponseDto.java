package com.likelion.ai_teacher_a.domain.image.dto;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageAndResponseDto {
    private Image image;                // 저장된 Image 엔티티
    private ImageResponseDto response; // 클라이언트 응답용 DTO
}
