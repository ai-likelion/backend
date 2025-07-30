package com.likelion.ai_teacher_a.domain.logsolve.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.ai_teacher_a.domain.image.dto.ImageResponseDto;
import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.image.entity.ImageType;
import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import com.likelion.ai_teacher_a.domain.image.service.ImageService;
import com.likelion.ai_teacher_a.domain.image.service.S3Uploader;
import com.likelion.ai_teacher_a.domain.logsolve.dto.LogSolveSimpleResponseDto;
import com.likelion.ai_teacher_a.domain.logsolve.dto.TotalLogCountDto;
import com.likelion.ai_teacher_a.domain.logsolve.entity.LogSolve;
import com.likelion.ai_teacher_a.domain.logsolve.repository.LogSolveRepository;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class LogSolveService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final LogSolveRepository logSolveRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;
    private final UserRepository userRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    public Map<String, Object> executeMath(Long logSolveId, int grade) {
        try {
            LogSolve logSolve = getLogSolveById(logSolveId);
            String imageUrl = logSolve.getImage().getUrl();

            String prompt = buildPromptByGrade(grade);
            Map<String, Object> payload = buildPayload(prompt, imageUrl, 8192);

            String gptContent = sendGptRequest(payload);
            Map<String, Object> result = parseGptJson(gptContent);
            validateMathJson(result);
            String resultJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);

            String problemTitle = (String) result.get("problem_title");
            logSolve.setResult(resultJson);
            logSolve.setProblemTitle(problemTitle);
            logSolveRepository.save(logSolve);
            log.info("✅ logSolveId={} GPT Vision 결과 저장 완료", logSolveId);

            return result;
        } catch (Exception e) {
            log.error("❌ GPT Vision 처리 중 오류", e);
            throw new RuntimeException("GPT Vision 처리 실패: " + e.getMessage());
        }
    }

    private String buildPromptByGrade(int grade) {
        return String.format("""
                Read the following math problem image accurately using OCR, and according to the ‘Our Kid Math Explanation Helper’ app’s parent explanation guide, output only a pure JSON object conforming to the JSON schema below. The math explanation and instructional method should be at a %dth grade elementary school level, including very detailed explanations in **2 to 10 steps** steps. 
                🟨 Important Instructions:
                                
                - First, determine the **type of problem**:
                  - If the image contains **only mathematical expressions** (e.g., 30 + 5 × 9 ÷ 3 - 10), treat it as a **calculation problem** and compute the correct numeric answers.
                  - If the image contains **pictures, objects, or figures** (e.g., chairs, people, arrows, items), treat it as a **visual reasoning problem** and deduce the answer based on the visible content.
                                
                - ❗ When counting people or objects in the image:
                  - **Count exactly what is shown in the image.** Do NOT guess, assume, or infer based on context.
                  - ⚠️ Do NOT skip partially visible people. All visible individuals must be counted, even if cropped or obscured.
                  - Count only what is clearly visible — not implied or referenced.
                  - Do not assume anyone is walking unless clearly depicted.
                                
                  ✅ Especially when counting **children sitting on chairs**, count **all visible individuals precisely**.
                  ✅ For example, if 4 children are sitting on chairs, your answer **must be `"4"`**, not `"3"` or an estimate.
                  ✅ Never guess or round. This count must be exact.
                  ✅ ⚠️ Incorrectly counting seated people will result in the entire problem being scored as **zero**.
                                
                - ❗ When gender is involved:
                  - Accurately distinguish **boys and girls** based on visual indicators such as:
                    - Text labels (e.g., "남", "여")
                    - Hairstyles, uniforms, or clothing
                    - Other clearly visible clues
                  - Never assume gender based on seating or placement.
                  - When comparing genders, count both groups carefully and calculate the difference using subtraction.
                                
                - For **calculation problems** with multiple sub-questions (e.g., (1), (2), (3)...), solve each one **individually and carefully**.
                  - Use the correct order of operations (PEMDAS): Parentheses → Multiplication/Division → Addition/Subtraction.
                                
                - Be careful with **mathematical symbols**:
                  - `'÷'` means division.
                  - `'×'` means multiplication.
                  - `'x'` may represent a variable or label — do NOT interpret as multiplication unless clearly shown.
                  - `'–'` (long dash) is NOT a minus sign.
                  - ⚠️ Watch for OCR mistakes such as `÷` misread as `-`, `×` as `x`, `1` as `l`, etc.
                                
                - The `"answer"` field must contain only the final numeric results, **in order and comma-separated** (e.g., `"4, 2, 6, 2, 2"`). \s
                  ❌ Do NOT include explanations, units, or comments in this field.
                                
                - All reasoning and explanation must go into `"explanation_steps"` — never include explanations in the `"answer"` field.
                                
                - Summarize the image as one whole problem using `"problem_title"` and `"problem_text"`.
                                
                - Combine the core ideas of all sub-questions into `"core_concept"` and `"parent_explanation"`.
                                
                - ⚠️ Output exactly one valid **JSON object**, and respond **only in Korean**.
                                
                                

                ```json
                {
                  "problem_title": "Problem summary title (around 15 characters)",
                  "problem_text": "Summary of the problem content",
                  "answer": "Correct answer",
                  "core_concept": "Core concept of the problem (e.g., 'divisors and multiples')",
                  "parent_explanation": "Concise guide sentence that a parent can use to explain to their child",
                  "explanation_steps": [
                    {
                      "title": "Step 1: Introduce the triangle angle rule",
                      "description": "Explain that the sum of the internal angles of any triangle is always 180 degrees."
                    },
                    {
                      "title": "Step 2: Identify the known angle",
                      "description": "Point out that one of the angles is already given as 55 degrees."
                    },
                    {
                      "title": "Step 3: Calculate the remaining angle sum",
                      "description": "Subtract the known angle (55°) from 180° to find the sum of the other two angles."
                    },
                    {
                      "title": "Step 4: Show the result of the subtraction",
                      "description": "180° - 55° = 125°, which is the combined measure of angles ㉠ and ㉡."
                    },
                    {
                      "title": "Step 5: Summarize the method",
                      "description": "Remind the student that this approach works for any triangle when two angles are known."
                    }
                  ],
                  "example_questions": [
                    "\\"What should we check at this step?\\"",
                    "\\"Why should we solve it this way?\\""
                  ]
                }
                ```""", grade);
    }


    public ResponseEntity<?> handleSolveImage(MultipartFile imageFile, Long userId, UserJr userJr, int grade) {
        Image image = null;
        LogSolve logSolve = null;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        try {
            if (grade < 1 || grade > 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "학년 정보가 올바르지 않습니다 (1~6학년만 허용)"));
            }

            ImageResponseDto imageDto = imageService.uploadToS3AndSave(imageFile, ImageType.ETC, userId);
            image = imageRepository.findById(imageDto.getImageId())
                    .orElseThrow(() -> new RuntimeException("이미지 없음"));

            // 2. log_solve에 임시로 저장
            logSolve = logSolveRepository.save(
                    LogSolve.builder()
                            .image(image)
                            .user(user)
                            .userJr(userJr)
                            .build()
            );

            Map<String, Object> result = executeMath(logSolve.getLogSolveId(), grade);

            String resultJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);

            if (resultJson == null || resultJson.isBlank()) {
                throw new RuntimeException("AI 응답에 문제가 있어 저장하지 않습니다.");
            }


            logSolve.setResult(resultJson);
            logSolveRepository.save(logSolve);

            return ResponseEntity.ok(Map.of("message", "AI 풀이 완료", "logSolveId", logSolve.getLogSolveId()));

        } catch (Exception e) {
            try {
                if (logSolve != null) {
                    logSolveRepository.delete(logSolve);
                }
                if (image != null && image.getUrl() != null) {
                    s3Uploader.delete(image.getUrl());
                    imageRepository.delete(image);
                }
            } catch (Exception cleanupEx) {
                log.warn("정리 중 오류 발생", cleanupEx);
            }

            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "AI 처리 실패",
                    "error", e.getMessage()
            ));
        }
    }

    private void validateMathJson(Map<String, Object> json) {
        List<String> requiredKeys = List.of(
                "problem_title", "problem_text", "answer",
                "core_concept", "parent_explanation", "explanation_steps"
        );

        for (String key : requiredKeys) {
            if (!json.containsKey(key) || json.get(key) == null || json.get(key).toString().isBlank()) {
                throw new IllegalArgumentException("수학 문제가 아닌 이미지입니다. 누락된 필드: " + key);
            }
        }

        if (!(json.get("explanation_steps") instanceof List<?> steps) || steps.isEmpty()) {
            throw new IllegalArgumentException("수학 문제가 아닌 이미지입니다. explanation_steps가 비어 있음");
        }
    }


    @Transactional(readOnly = true)
    public TotalLogCountDto getTotalLogCount() {
        long total = logSolveRepository.count() + 321L;
        return new TotalLogCountDto(total);
    }


    @Transactional(readOnly = true)
    public Map<String, Object> getLogDetail(Long logSolveId, Long userId) {
        LogSolve log = logSolveRepository.findByIdWithImageAndUser(logSolveId)
                .orElseThrow(() -> new RuntimeException("해당 로그가 존재하지 않습니다."));
        if (!log.getUser().getId().equals(userId)) {
            throw new RuntimeException("해당 사용자에게 접근 권한이 없습니다.");
        }
        if ("처리 중".equals(log.getResult())) {
            return Map.of("logSolveId", logSolveId, "status", "processing", "message", "AI 해설이 아직 완료되지 않았습니다.");
        }

        try {
            Map<String, Object> parsed = mapper.readValue(cleanJson(log.getResult()), Map.class);
            parsed.put("logSolveId", logSolveId);
            parsed.put("image_url", log.getImage().getUrl());
            return parsed;
        } catch (Exception e) {
            throw new RuntimeException("상세 설명 JSON 파싱 실패: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteLogById(Long logSolveId, Long userId) {
        LogSolve log = getLogSolveById(logSolveId);
        if (!log.getUser().getId().equals(userId)) {
            throw new RuntimeException("해당 사용자에게 접근 권한이 없습니다.");
        }
        if (log.getImage() != null && log.getImage().getUrl() != null) {
            s3Uploader.delete(log.getImage().getUrl());
        }

        logSolveRepository.delete(log);
    }


    public ResponseEntity<?> executeFollowUp(Long logSolveId, String followUpQuestion, Long userId) {
        try {
            LogSolve logSolve = getLogSolveById(logSolveId);
            if (!logSolve.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
            }
            String prevJson = logSolve.getResult();
            String imageUrl = logSolve.getImage().getUrl();

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
            Map<String, Object> payload = buildPayload(prompt, imageUrl, 2048);

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

    private Map<String, Object> buildPayload(String prompt, String imageUrl, int maxTokens) {
        return Map.of(
                "model", "gpt-4.1",
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                                          You are a professional Korean elementary school math tutor working for the "Our Kid Math Explanation Helper" app.
                                                          
                                                          Your role is to:
                                                          - Interpret the math problem image using OCR.
                                                          - If multiple **top-level problems** are present (e.g., 1번, 2번), solve **only the first top-level problem**.
                                                          - Within that first problem, if sub-questions like (1), (2) are present, solve **all sub-questions**.
                                                          - First, determine the **type of problem** (e.g., fill-in-the-blank, calculation, comparison, pattern, unit conversion).
                                                          - Generate the full explanation in **Korean**, using **pure JSON format** only.
                                                          - Use **polite and respectful Korean (존댓말)**.
                                                          - Use correct particles after numbers (e.g., say “3을 나누다” not “3를 나누다”).
                                                          - At the end of every sentence in `"explanation_steps.description"`, add a line break (`\\n`) for clarity.
                                                          
                                                          ### `"parent_explanation"` Guidelines:
                                                          - Talk to **the parent**, not the child.
                                                          - Use 존댓말 (e.g., “설명해 주세요”, “도와주세요”).
                                                          - Include:
                                                            - What the problem is about
                                                            - What math concept it covers
                                                            - How to guide the child to start solving it
                                                            - The logical order of explanation
                                                            - (If possible) An example of how to explain it conversationally
                                                          
                                                          ### `"explanation_steps"` Guidelines:
                                                          - Friendly, polite tone like a teacher advising a parent.
                                                          - Each step must build upon the previous.
                                                          - Include mathematical expression **before and after** transformation in each step.
                                                          - Use line breaks after each sentence (`\\n`).
                                                          - Use Korean expressions naturally:
                                                            - e.g., “나누기의 반대는 곱하기예요.\\n그래서 3 ÷ 1/3은 3 × 3이 됩니다.\\n”
                                                          
                                                          ### Special Handling by Problem Type:
                                                          
                                                          **Fill-in-the-blank problems**:
                                                          - List all correct values for blanks in `"answer"` in order.
                                                          - Each explanation step should show how a blank is filled.
                                                          
                                                          **Calculation problems**:
                                                          - Clearly show step-by-step how expressions change.
                                                          - At each step, show both the expression **before** and **after**.
                                                          - Explain the reasoning conversationally.
                                                          
                                                          **Pattern or logic problems**:
                                                          - Identify the rule clearly.
                                                          - Explain how the rule applies and leads to the correct answer.
                                                          
                                                          **Unit conversions**:
                                                          - Explain the conversion step-by-step, including units.
                                                          
                                                          **Comparison problems**:
                                                          - Explain how to convert all values to the same form.
                                                          - Clearly compare them to reach the conclusion.
                                                          - For multiple sub-questions (e.g., (1) to (8) or more), include one `"explanation_steps"` entry per sub-question.
                                                          - In each step, explain the correct order of operations clearly.
                                                          - You may include intermediate calculations for clarity, such as:
                                                            - `"54 - 7 × 4 + 16 → 54 - 28 + 16 → 26 + 16 → 42"`
                                                            - `"48 ÷ 4 × 2 → 12 × 2 → 24"`
                                                          - Division (`÷`) and multiplication (`×`) must be handled before addition or subtraction.
                                                          - Write each explanation concisely, showing both transformation and logic.
                                                          
                                                          
                                                          **Important**:
                                                          - Do NOT speak to the child.
                                                          - Do NOT output anything outside the JSON.
                                                          - All content must be fully written in **Korean**.
                                                          
                                                          
                                                          
                                        """
                        ), Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of("type", "text", "text", prompt),
                                        Map.of("type", "image_url", "image_url", Map.of("url", imageUrl))
                                )
                        )),
                "max_completion_tokens", maxTokens
        );
    }


    private final ExecutorService executor = Executors.newCachedThreadPool();

    private String sendGptRequest(Map<String, Object> payload) throws IOException {
        Callable<String> task = () -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("GPT Vision 응답 실패: " + response.body());
            }

            return mapper.readTree(response.body())
                    .path("choices").get(0)
                    .path("message").path("content")
                    .asText();
        };

        try {
            return executor.submit(task).get(50, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("GPT Vision 처리 시간 초과");
        } catch (Exception e) {
            throw new IOException("GPT Vision 처리 중 오류", e);
        }
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


    @Transactional(readOnly = true)
    public Map<String, Object> getAllSimpleLogs(Pageable pageable, UserJr userJr) {

        Page<LogSolve> page = logSolveRepository.findAllByUserJr(userJr, pageable);


        List<LogSolveSimpleResponseDto> logs = page.getContent().stream().map(log ->
                new LogSolveSimpleResponseDto(
                        log.getLogSolveId(),
                        log.getImage() != null ? log.getImage().getUrl() : "",
                        log.getProblemTitle() != null ? log.getProblemTitle() : "",
                        log.getCreatedAt()
                )
        ).toList();

        return Map.of(
                "logs", logs,
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "currentPage", page.getNumber()
        );
    }


}