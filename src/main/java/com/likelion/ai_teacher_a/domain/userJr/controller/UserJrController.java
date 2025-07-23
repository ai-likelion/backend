package com.likelion.ai_teacher_a.domain.userJr.controller;

import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrResponseDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrUpdateRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.service.UserJrService;
import com.likelion.ai_teacher_a.global.auth.resolver.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "User-Jr", description = "사용자 자녀 관련 API")
@RestController
@RequestMapping("/api/user-jrs")
@RequiredArgsConstructor
public class UserJrController {

    private final UserJrService userJrService;

    @Operation(summary = "사용자 자녀 등록 (프로필 이미지 포함)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserJrResponseDto> create(
            @LoginUserId Long userId,
            @RequestPart("metadata") @Valid UserJrRequestDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.ok(userJrService.create(dto, image, userId));
    }


    @Operation(summary = "특정 자녀 조회")
    @GetMapping("/{id}")
    public ResponseEntity<UserJrResponseDto> get(@LoginUserId Long userId,
                                                 @PathVariable Long id) {
        return ResponseEntity.ok(userJrService.findById(id, userId));
    }

    @Operation(summary = "사용자 기준 자녀 목록 조회")
    @GetMapping("/parent")
    public ResponseEntity<List<UserJrResponseDto>> listByParent(@LoginUserId Long userId) {
        return ResponseEntity.ok(userJrService.findByParent(userId));
    }

    @Operation(summary = "자녀 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@LoginUserId Long userId,
                                       @PathVariable Long id) {
        userJrService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "자녀 정보 수정 (이미지 포함 가능)")
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUserJr(
            @LoginUserId Long userId,
            @PathVariable Long id,
            @RequestPart("metadata") @Valid UserJrUpdateRequestDto dto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        userJrService.updateUserJr(id, dto, userId, imageFile); // ⬅️ 이미지도 넘김
        return ResponseEntity.ok().build();
    }


}
