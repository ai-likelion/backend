package com.likelion.ai_teacher_a.domain.image.controller;

import com.likelion.ai_teacher_a.domain.image.dto.ImageResponseDto;
import com.likelion.ai_teacher_a.domain.image.entity.ImageType;
import com.likelion.ai_teacher_a.domain.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Tag(name = "이미지 업로드", description = "이미지를 S3에 업로드하고 URL을 조회하는 API")
@RestController
@RequestMapping("/api/public/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;


    @Operation(summary = "이미지를 S3에 업로드", description = "MultipartFile 형태로 이미지를 업로드하고 S3 URL과 정보를 반환합니다.")
    @PostMapping("/upload")
    public ResponseEntity<ImageResponseDto> uploadToS3(@RequestParam("file") MultipartFile file) throws IOException {
        ImageResponseDto dto = imageService.uploadToS3AndSave(file, ImageType.ETC);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "이미지 URL 조회", description = "이미지 ID를 이용해 S3에 저장된 이미지의 URL을 반환합니다.")
    @GetMapping("/{imageId}")
    public ResponseEntity<Map<String, Object>> getImageUrl(@PathVariable("imageId") Long imageId) {
        String url = imageService.getImageUrl(imageId);
        return ResponseEntity.ok(Map.of("imageId", imageId, "url", url));
    }
}

