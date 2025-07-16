package com.likelion.ai_teacher_a.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ImageResponseDto {
    private Long imageId;
    private String fileName;
    private Integer fileSize;
    private String url;
    private LocalDateTime uploadedAt;

}