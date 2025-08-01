package com.likelion.ai_teacher_a.domain.logsolve.dto;

import com.likelion.ai_teacher_a.domain.image.dto.ImageResponseDto;

import java.time.LocalDateTime;
import java.util.Map;

public record LogSolveResponseDto(
        Long logSolveId,
        ImageResponseDto image,
        Map<String, Object> result,
        LocalDateTime uploadedAt
) {
}

