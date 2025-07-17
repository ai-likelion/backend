package com.likelion.ai_teacher_a.domain.userJr.controller;

import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrResponseDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrUpdateRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.service.UserJrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User-Jr", description = "사용자 자녀 관련 API")
@RestController
@RequestMapping("/api/user-jrs")
@RequiredArgsConstructor
public class UserJrController {

    private final UserJrService userJrService;

    @Operation(summary = "사용자 자녀 등록")
    @PostMapping
    public ResponseEntity<UserJrResponseDto> create(@RequestBody @Valid UserJrRequestDto dto) {
        return ResponseEntity.ok(userJrService.create(dto));
    }

    @Operation(summary = "특정 자녀 조회")
    @GetMapping("/{id}")
    public ResponseEntity<UserJrResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(userJrService.findById(id));
    }

    @Operation(summary = "사용자 기준 자녀 목록 조회")
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<UserJrResponseDto>> listByParent(@PathVariable Long parentId) {
        return ResponseEntity.ok(userJrService.findByParent(parentId));
    }

    @Operation(summary = "자녀 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userJrService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "자녀 프로필 이미지 설정")
    @PatchMapping("/{userJrId}/profile-image")
    public ResponseEntity<Void> setProfileImage(
            @PathVariable Long userJrId,
            @RequestParam Long imageId
    ) {
        userJrService.setProfileImage(userJrId, imageId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자녀 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateUserJr(
            @PathVariable Long id,
            @RequestBody UserJrUpdateRequestDto dto
    ) {
        userJrService.updateUserJr(id, dto);
        return ResponseEntity.ok().build();
    }
}
