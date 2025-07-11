package com.likelion.ai_teacher_a.domain.logsolve.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class LogSolveService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final LogSolveRepository logSolveRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    public Map<String, Object> explainMathImage(MultipartFile imageFile) throws IOException, InterruptedException {
        // ✅ 1. 이미지 저장
        ImageResponseDto imageDto = imageService.upload(imageFile, ImageType.ETC);
        byte[] imageBytes = imageService.getImageData(imageDto.getImageId());
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // ✅ 2. 프롬프트 구성
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


        Map<String, Object> payload = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", prompt),
                                Map.of("type", "image_url", "image_url", Map.of(
                                        "url", "data:image/jpeg;base64," + base64Image
                                ))
                        ))
                ),
                "max_tokens", 8192
        );


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());


        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());
        JsonNode choices = json.path("choices");
        if (!choices.isArray() || choices.size() == 0) {
            throw new RuntimeException("GPT 응답 실패: " + response.body());
        }

        String gptContent = choices.get(0).path("message").path("content").asText();


        String cleanedJson = gptContent
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();


        Map<String, Object> resultJson;
        try {
            resultJson = mapper.readValue(cleanedJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답이 올바른 JSON이 아닙니다:\n" + gptContent);
        }


        String resultJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultJson);


        Image image = imageRepository.findById(imageDto.getImageId())
                .orElseThrow(() -> new RuntimeException("Image not found"));

        logSolveRepository.save(LogSolve.builder()
                .image(image)
                .result(resultJsonString)
                .build());


        return resultJson;
    }


    public PagedLogResponseDto getAllLogs(Pageable pageable) {
        Page<LogSolve> logsPage = logSolveRepository.findAll(pageable);
        ObjectMapper mapper = new ObjectMapper();

        List<LogSolveResponseDto> logs = logsPage.stream()
                .map(log -> {
                    String rawResult = log.getResult();


                    String cleanedJson = rawResult
                            .replaceAll("^```json\\s*", "")
                            .replaceAll("^```\\s*", "")
                            .replaceAll("\\s*```$", "")
                            .trim();

                    Map<String, Object> parsedResult;
                    try {
                        parsedResult = mapper.readValue(cleanedJson, Map.class);
                    } catch (Exception e) {

                        parsedResult = Map.of("error", "Invalid JSON in DB", "raw", rawResult);
                    }

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
                })
                .toList();

        return new PagedLogResponseDto(
                logs,
                logsPage.getTotalElements(),
                logsPage.getTotalPages(),
                logsPage.getNumber()
        );
    }


    public Map<String, Object> getLogDetail(Long logSolveId) {
        LogSolve log = logSolveRepository.findById(logSolveId)
                .orElseThrow(() -> new IllegalArgumentException("해당 로그가 존재하지 않습니다."));

        String result = log.getResult();


        String cleanedJson = result
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();

        try {
            return new ObjectMapper().readValue(cleanedJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("상세 설명 JSON 파싱 실패: " + result);
        }
    }


}
