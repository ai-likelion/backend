package com.likelion.ai_teacher_a.domain.logsolve.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.ai_teacher_a.domain.image.dto.ImageResponseDto;
import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.image.entity.ImageType;
import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import com.likelion.ai_teacher_a.domain.image.service.ImageService;
import com.likelion.ai_teacher_a.domain.logsolve.dto.LogSolveResponseDto;
import com.likelion.ai_teacher_a.domain.logsolve.dto.PagedLogResponseDto;
import com.likelion.ai_teacher_a.domain.logsolve.entity.LogSolve;
import com.likelion.ai_teacher_a.domain.logsolve.repository.LogSolveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LogSolveService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final LogSolveRepository logSolveRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();


    public Map<String, Object> executeMath(Long logSolveId) {
        try {
            LogSolve logSolve = getLogSolveById(logSolveId);
            String base64Image = encodeImageToBase64(logSolve.getImage().getImageId());

            String prompt = """
                    Read the following math problem image accurately using OCR, and according to the ‘Our Kid Math Explanation Helper’ app’s parent explanation guide, output only a pure JSON object conforming to the JSON schema below. The math explanation and instructional method should be at a 6th grade elementary school level, including very detailed explanations in 4–6 steps. Since real-time web search for visual aids is not possible, present visual aid suggestions as search keywords and example URLs (placeholders). Please respond only in Korean.

                    ```json
                    {
                      "problem_title": "Problem summary title (around 15 characters)",
                      "problem_text": "Summary of the problem content",
                      "answer": "Correct answer",
                      "core_concept": "Core concept of the problem (e.g., 'divisors and multiples')",
                      "parent_explanation": "Concise guide sentence that a parent can use to explain to their child",
                      "explanation_steps": [
                        "Step 1: …",
                        "Step 2: …",
                        "Step 3: …",
                        "Step 4: …",
                        "Step 5: …"
                      ],
                      "visual_aid": [
                        {
                          "title": "Example visual aid title",
                          "search_term": "Example search keyword",
                          "example_url": "https://placeholder.example.com/your-image.png"
                        },
                        {
                          "title": "Another visual aid title",
                          "search_term": "Another search keyword",
                          "example_url": "https://placeholder.example.com/another-image.png"
                        }
                      ],
                      "example_questions": [
                        "\\"What should we check at this step?\\"",
                        "\\"Why should we solve it this way?\\""
                      ]
                    }
                    ```""";
            Map<String, Object> payload = buildPayload(prompt, base64Image, 8192);

            String gptContent = sendGptRequest(payload);
            Map<String, Object> result = parseGptJson(gptContent);
            String resultJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);

            logSolve.setResult(resultJson);
            logSolveRepository.save(logSolve);
            log.info("✅ logSolveId={} GPT Vision 결과 저장 완료", logSolveId);

            return result;
        } catch (Exception e) {
            log.error("❌ GPT Vision 처리 중 오류", e);
            throw new RuntimeException("GPT Vision 처리 실패: " + e.getMessage());
        }
    }

    public ResponseEntity<?> handleSolveImage(MultipartFile imageFile) {
        try {
            Long logSolveId = createLogAndReturnId(imageFile);
            executeMath(logSolveId);
            return ResponseEntity.ok(Map.of("message", "AI 풀이 완료", "logSolveId", logSolveId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "AI 처리 실패", "error", e.getMessage()));
        }
    }

    public Long createLogAndReturnId(MultipartFile imageFile) throws IOException {
        ImageResponseDto imageDto = imageService.upload(imageFile, ImageType.ETC);
        Image image = imageRepository.findById(imageDto.getImageId()).orElseThrow(() -> new RuntimeException("이미지 없음"));

        return logSolveRepository.save(LogSolve.builder().image(image).result("처리 중").build()).getLogSolveId();
    }

    @Transactional(readOnly = true)
    public PagedLogResponseDto getAllLogs(Pageable pageable) {
        Page<LogSolve> logsPage = logSolveRepository.findAll(pageable);
        List<LogSolveResponseDto> logs = logsPage.stream().map(this::convertLogToDto).toList();
        return new PagedLogResponseDto(logs, logsPage.getTotalElements(), logsPage.getTotalPages(), logsPage.getNumber());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLogDetail(Long logSolveId) {
        LogSolve log = getLogSolveById(logSolveId);

        if ("처리 중".equals(log.getResult())) {
            return Map.of("logSolveId", logSolveId, "status", "processing", "message", "AI 해설이 아직 완료되지 않았습니다.");
        }

        try {
            Map<String, Object> parsed = mapper.readValue(cleanJson(log.getResult()), Map.class);
            parsed.put("logSolveId", logSolveId);
            return parsed;
        } catch (Exception e) {
            throw new RuntimeException("상세 설명 JSON 파싱 실패: " + e.getMessage());
        }
    }

    public void deleteLogById(Long logSolveId) {
        LogSolve log = getLogSolveById(logSolveId);
        logSolveRepository.delete(log);
    }

    public ResponseEntity<?> executeFollowUp(Long logSolveId, String followUpQuestion) {
        try {
            LogSolve logSolve = getLogSolveById(logSolveId);
            String prevJson = logSolve.getResult();
            String base64Image = encodeImageToBase64(logSolve.getImage().getImageId());

            String prompt = String.format("""
                    You are an AI math explanation assistant for elementary school students.

                    Below is a math problem image and the previous explanation (in JSON) that the AI provided.

                    A parent has asked the following **follow-up question** because the child is still confused:

                    🟨🟨🟨 Please answer this question specifically and clearly! 🟨🟨🟨  
                    ➡️ Follow-up question: **%s**

                    This question is an attempt by the student to understand the problem more deeply and should be addressed clearly.

                    ---

                    Previous explanation JSON:
                    %s

                    ---

                    📌 Please respond only in Korean and return only a JSON object in the following format:
                    ```json
                    {
                      "answers_to_additional_questions": [
                        "Write accurate and detailed answers to follow-up questions.",
                        "Provide an explanation that matches the intent of the question, without using any illustrations"
                      ]
                    }
                    ```            
                    """, followUpQuestion, prevJson);
            Map<String, Object> payload = buildPayload(prompt, base64Image, 2048);

            String gptContent = sendGptRequest(payload);
            Map<String, Object> additional = parseGptJson(gptContent);
            Map<String, Object> prevResult = mapper.readValue(prevJson, Map.class);

            prevResult.put("answers_to_additional_questions", additional.get("answers_to_additional_questions"));
            logSolve.setResult(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(prevResult));
            logSolveRepository.save(logSolve);


            return ResponseEntity.ok(Map.of("message", "AI 추가 질문 풀이 완료", "logSolveId", logSolveId));
        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(Map.of("message", "추가 질문 처리 실패", "error", e.getMessage()));
        }
    }


    private LogSolve getLogSolveById(Long id) {
        return logSolveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 수학문제 해설이 없습니다."));
    }


    private String encodeImageToBase64(Long imageId) throws IOException {
        return Base64.getEncoder().encodeToString(imageService.getImageData(imageId));
    }

    private Map<String, Object> buildPayload(String prompt, String base64Image, int maxTokens) {
        return Map.of(
                "model", "gpt-4o",
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", prompt),
                                Map.of("type", "image_url", "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image))
                        )
                )),
                "max_tokens", maxTokens
        );
    }

    private String sendGptRequest(Map<String, Object> payload) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("GPT Vision 응답 실패: " + response.body());
        }

        return mapper.readTree(response.body()).path("choices").get(0).path("message").path("content").asText();
    }

    private Map<String, Object> parseGptJson(String raw) throws IOException {
        return mapper.readValue(cleanJson(raw), Map.class);
    }

    private String cleanJson(String raw) {
        return Optional.ofNullable(raw).orElse("")
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();
    }

    private LogSolveResponseDto convertLogToDto(LogSolve log) {
        try {
            Map<String, Object> parsedResult = mapper.readValue(cleanJson(log.getResult()), Map.class);
            return new LogSolveResponseDto(
                    log.getLogSolveId(),
                    new ImageResponseDto(
                            log.getImage().getImageId(),
                            log.getImage().getFileName(),
                            log.getImage().getFileSize(),
                            log.getImage().getUploadedAt()
                    ),
                    parsedResult,
                    log.getImage().getUploadedAt()
            );
        } catch (Exception e) {
            return new LogSolveResponseDto(log.getLogSolveId(), null,
                    Map.of("error", "JSON 파싱 실패", "raw", log.getResult()),
                    log.getImage().getUploadedAt());
        }
    }
}