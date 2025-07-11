package com.likelion.ai_teacher_a.domain.image.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ImageUploadRequestDto {
    private MultipartFile file;
}
