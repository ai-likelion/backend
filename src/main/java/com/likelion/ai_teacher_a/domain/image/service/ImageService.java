package com.likelion.ai_teacher_a.domain.image.service;

import com.likelion.ai_teacher_a.domain.image.dto.ImageAndResponseDto;
import com.likelion.ai_teacher_a.domain.image.dto.ImageResponseDto;
import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.image.entity.ImageType;
import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;


    @Transactional
    public ImageResponseDto uploadToS3AndSave(MultipartFile file, ImageType type, Long userId) throws IOException {
        User user = userRepository.getReferenceById(userId);

        String url = s3Uploader.upload(file);


        Image image = Image.builder()
                .fileName(file.getOriginalFilename())
                .fileSize((int) file.getSize())// 바이너리 저장 안 함
                .type(type)
                .url(url)
                .user(user)
                .build();

        imageRepository.save(image);


        return new ImageResponseDto(
                image.getImageId(),
                image.getFileName(),
                image.getFileSize(),
                image.getUrl(),
                image.getUploadedAt()
        );

    }


    @Transactional
    public void deleteImage(Long imageId, Long userId) {
        User user = userRepository.getReferenceById(userId);
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));


        if (image.getUrl() != null) {
            s3Uploader.delete(image.getUrl());
        }

        imageRepository.delete(image);
    }


    @Transactional(readOnly = true)
    public String getImageUrl(Long imageId, Long userId) {
        User user = userRepository.getReferenceById(userId);
        Image image = imageRepository.findByImageIdAndUser(imageId, user)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));
        return image.getUrl();
    }


    @Transactional
    public ImageAndResponseDto uploadToS3AndSaveWithEntity(MultipartFile file, ImageType type, Long userId) throws IOException {
        User user = userRepository.getReferenceById(userId);
        String url = s3Uploader.upload(file);

        Image image = Image.builder()
                .fileName(file.getOriginalFilename())
                .fileSize((int) file.getSize())
                .type(type)
                .url(url)
                .user(user)
                .build();

        imageRepository.save(image);

        ImageResponseDto response = new ImageResponseDto(
                image.getImageId(),
                image.getFileName(),
                image.getFileSize(),
                image.getUrl(),
                image.getUploadedAt()
        );

        return new ImageAndResponseDto(image, response);
    }


}
