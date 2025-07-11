package com.likelion.ai_teacher_a.domain.image.service;

import com.likelion.ai_teacher_a.domain.image.dto.ImageResponseDto;
import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.image.entity.ImageType;
import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageResponseDto upload(MultipartFile file, ImageType type) throws IOException {
        Image image = new Image();
        image.setFileName(file.getOriginalFilename());
        image.setFileSize((int) file.getSize());
        image.setType(type);
        image.setData(file.getBytes());

        Image saved = imageRepository.save(image);
        return new ImageResponseDto(
                saved.getImageId(),
                saved.getFileName(),
                saved.getFileSize(),
                saved.getUploadedAt()
        );
    }

    public byte[] getImageData(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        return image.getData();
    }
}

