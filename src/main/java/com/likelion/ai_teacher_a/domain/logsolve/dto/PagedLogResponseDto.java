package com.likelion.ai_teacher_a.domain.logsolve.dto;

import java.util.List;

public record PagedLogResponseDto(
        List<LogSolveResponseDto> logs,
        long totalElements,
        int totalPages,
        int currentPage
) {
}
