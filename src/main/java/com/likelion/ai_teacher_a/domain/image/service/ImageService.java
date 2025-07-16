package com.likelion.ai_teacher_a.domain.image.service;

import com.likelion.ai_teacher_a.domain.image.dto.ImageResponseDto;
import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.image.entity.ImageType;
import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    private final S3Uploader s3Uploader;


    @Transactional
    public ImageResponseDto uploadToS3AndSave(MultipartFile file, ImageType type) throws IOException {
        // ✅ 1. S3 업로드
        String url = s3Uploader.upload(file);

        // ✅ 2. DB 저장
        Image image = Image.builder()
                .fileName(file.getOriginalFilename())
                .fileSize((int) file.getSize())// 바이너리 저장 안 함
                .type(type)
                .url(url)
                .build();

        imageRepository.save(image);

        // ✅ 3. DTO 반환
        return new ImageResponseDto(
                image.getImageId(),
                image.getFileName(),
                image.getFileSize(),
                image.getUrl(),            // ✅ 4번째: url
                image.getUploadedAt()      // ✅ 5번째: uploadedAt
        );

    }


    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

        // S3에서 삭제
        if (image.getUrl() != null) {
            s3Uploader.delete(image.getUrl());
        }

        // DB에서 삭제
        imageRepository.delete(image);
    }


    @Transactional(readOnly = true)
    public String getImageUrl(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));
        return image.getUrl();
    }


}
