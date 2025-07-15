package com.likelion.ai_teacher_a.domain.logsolve.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.ai_teacher_a.domain.logsolve.service.LogSolveService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Math AI 문제해설", description = "AI 이미지 문제 해설 및 로그 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/math")
public class LogSolvePublicController {

	private final LogSolveService logSolveService;

	@Operation(summary = "해설 로그 전체 개수 조회")
	@GetMapping("/count")
	public ResponseEntity<?> getAllLogCount() {
		return ResponseEntity.ok(logSolveService.getLogCount());
	}
}
