package com.likelion.ai_teacher_a.domain.logsolve.controller;

import com.likelion.ai_teacher_a.domain.logsolve.dto.TotalLogCountDto;
import com.likelion.ai_teacher_a.domain.logsolve.service.LogSolveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "Math AI 문제해설", description = "AI 이미지 문제 해설 및 로그 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/math")
public class LogSolveController {

    private final LogSolveService logSolveService;

    @Operation(summary = "이미지 문제 업로드 및 AI 해설 시작")
    @PostMapping(value = "/solve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> solveImage(@Parameter(description = "문제 이미지 파일") @RequestParam("mathProblemImage") MultipartFile image) {
        return logSolveService.handleSolveImage(image);
    }

    @Operation(summary = "해설 로그 전체 목록 조회 (페이징)")
    @GetMapping
    public ResponseEntity<?> getAllLogs(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "3") @RequestParam(name = "size", defaultValue = "3") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "image.uploadedAt"));
        return ResponseEntity.ok(logSolveService.getAllLogs(pageable));
    }

    @Operation(summary = "단일 문제해설 상세 조회")
    @GetMapping("/{logSolveId}")
    public ResponseEntity<?> getLogDetail(@Parameter(description = "해당 문제해설 로그 ID") @PathVariable("logSolveId") Long logSolveId) {
        try {
            Map<String, Object> result = logSolveService.getLogDetail(logSolveId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "문제해설 로그 삭제")
    @DeleteMapping("/{logSolveId}")
    public ResponseEntity<?> deleteLog(@Parameter(description = "삭제할 로그 ID") @PathVariable("logSolveId") Long logSolveId) {
        try {
            logSolveService.deleteLogById(logSolveId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "AI 추가 질문 요청 (Follow-up)")
    @PostMapping("/ask-again/{logSolveId}")
    public ResponseEntity<?> askAgain(
            @Parameter(description = "기존 해설 로그 ID") @PathVariable("logSolveId") Long logSolveId,
            @Parameter(description = "사용자의 추가 질문") @RequestParam("question") String question
    ) {
        return logSolveService.executeFollowUp(logSolveId, question);
    }


    @Operation(
            summary = "전체 문제해설 로그 수 조회",
            description = "DB에 저장된 전체 LogSolve(문제해설 로그)의 총 개수를 반환합니다."
    )
    @GetMapping("/logs-total")
    public ResponseEntity<TotalLogCountDto> getTotalLogCount() {
        TotalLogCountDto total = logSolveService.getTotalLogCount();
        return ResponseEntity.ok(total);
    }


}
