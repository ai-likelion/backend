package com.likelion.ai_teacher_a.domain.userJr.controller;

import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrResponseDto;
import com.likelion.ai_teacher_a.domain.userJr.service.UserJrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-jrs")
@RequiredArgsConstructor
public class UserJrController {

    private final UserJrService userJrService;

    @PostMapping
    public ResponseEntity<UserJrResponseDto> create(@RequestBody UserJrRequestDto dto) {
        return ResponseEntity.ok(userJrService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserJrResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(userJrService.findById(id));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<UserJrResponseDto>> listByParent(@PathVariable Long parentId) {
        return ResponseEntity.ok(userJrService.findByParent(parentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userJrService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userJrId}/profile-image")
    public ResponseEntity<Void> setProfileImage(
            @PathVariable Long userJrId,
            @RequestParam Long imageId
    ) {
        userJrService.setProfileImage(userJrId, imageId);
        return ResponseEntity.ok().build();
    }
}

