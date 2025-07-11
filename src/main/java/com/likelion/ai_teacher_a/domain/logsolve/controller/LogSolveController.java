package com.likelion.ai_teacher_a.domain.logsolve.controller;

import com.likelion.ai_teacher_a.domain.logsolve.service.LogSolveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/math")
public class LogSolveController {

    private final LogSolveService gptVisionService;

    @PostMapping(value = "/solve-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> solveImage(@RequestParam("image") MultipartFile image) throws IOException, InterruptedException {
        Map<String, Object> result = gptVisionService.explainMathImage(image);
        return ResponseEntity.ok(result);
    }


    @GetMapping
    public ResponseEntity<?> getAllLogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "image.uploadedAt"));
        return ResponseEntity.ok(gptVisionService.getAllLogs(pageable));
    }


    @GetMapping("/{logSolveId}")
    public ResponseEntity<?> getLogDetail(@PathVariable("logSolveId") Long logSolveId) {
        return ResponseEntity.ok(gptVisionService.getLogDetail(logSolveId));
    }


}
