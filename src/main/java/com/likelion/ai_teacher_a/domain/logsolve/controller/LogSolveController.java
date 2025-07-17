package com.likelion.ai_teacher_a.domain.logsolve.controller;

import com.likelion.ai_teacher_a.domain.logsolve.dto.TotalLogCountDto;
import com.likelion.ai_teacher_a.domain.logsolve.service.LogSolveService;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.global.auth.resolver.annotation.LoginUserId;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "Math AI 문제해설", description = "AI 이미지 문제 해설 및 로그 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/math")
public class LogSolveController {

    private final LogSolveService logSolveService;
    private final UserRepository userRepository;

    @Operation(summary = "이미지 문제 업로드 및 AI 해설 시작")
    @PostMapping(value = "/solve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> solveImage(
            @Parameter(description = "문제 이미지 파일") @RequestParam("mathProblemImage") MultipartFile image,
            @Parameter(description = "자녀 학년 (1~6)") @RequestParam("grade") int grade,
            @LoginUserId Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

        return logSolveService.handleSolveImage(image, user, grade);
    }


    @Operation(summary = "단일 문제해설 상세 조회")
    @GetMapping("/{logSolveId}")
    public ResponseEntity<?> getLogDetail(@Parameter(description = "해당 문제해설 로그 ID") @PathVariable("logSolveId") Long logSolveId,
                                          @LoginUserId Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        try {
            Map<String, Object> result = logSolveService.getLogDetail(logSolveId, user);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "문제해설 로그 삭제")
    @DeleteMapping("/{logSolveId}")
    public ResponseEntity<?> deleteLog(@Parameter(description = "삭제할 로그 ID") @PathVariable("logSolveId") Long logSolveId,
                                       @LoginUserId Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        try {
            logSolveService.deleteLogById(logSolveId, user);
            return ResponseEntity.ok(Map.of("message", "삭제가 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "AI 추가 질문 요청 (Follow-up)")
    @PostMapping("/ask-again/{logSolveId}")
    public ResponseEntity<?> askAgain(
            @Parameter(description = "기존 해설 로그 ID") @PathVariable("logSolveId") Long logSolveId,
            @Parameter(description = "사용자의 추가 질문") @RequestParam("question") String question,
            @LoginUserId Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        return logSolveService.executeFollowUp(logSolveId, question, user);
    }


    @Operation(
            summary = "전체 문제해설 로그 수 조회",
            description = "DB에 저장된 전체 LogSolve(문제해설 로그)의 총 개수를 반환합니다."
    )
    @GetMapping("/logs-total")
    public ResponseEntity<TotalLogCountDto> getTotalLogCount(@LoginUserId Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        TotalLogCountDto total = logSolveService.getTotalLogCount(user);
        return ResponseEntity.ok(total);
    }


    @Operation(summary = "모든 문제해설 요약 목록 조회 (imageUrl + title, 전체)")
    @GetMapping("/all-logs")
    public ResponseEntity<?> getSimpleLogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @LoginUserId Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "image.uploadedAt")); // ✅ 핵심!
        return ResponseEntity.ok(logSolveService.getAllSimpleLogs(pageable, user));
    }


}

