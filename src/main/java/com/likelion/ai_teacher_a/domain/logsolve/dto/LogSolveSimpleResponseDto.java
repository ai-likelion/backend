package com.likelion.ai_teacher_a.domain.logsolve.dto;


import java.time.LocalDateTime;

public record LogSolveSimpleResponseDto(
        Long logSolveId,
        String imageUrl,
        String problemTitle,
        LocalDateTime uploadedAt
) {}
