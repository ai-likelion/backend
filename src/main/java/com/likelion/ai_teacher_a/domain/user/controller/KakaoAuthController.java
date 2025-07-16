package com.likelion.ai_teacher_a.domain.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.ai_teacher_a.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "Kakao Controller", description = "카카오 로그인 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth/kakao")
public class KakaoAuthController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUriFromApp;

    @Value("${frontend.redirect-uri}")
    private String frontendRedirectUri; // e.g., http://localhost:3000/login/success

    /* 카카오 로그인 URL로 리디렉트 */
    @Operation(summary = "카카오 로그인 URL로 리다이렉트")
    @GetMapping("/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String redirectUri = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUriFromApp
                + "&response_type=code";
        response.sendRedirect(redirectUri);
    }

    @Operation(summary = "카카오 로그인 콜백")
    @GetMapping("/callback")
    public void kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        try {
            // 1. 카카오 access_token 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("redirect_uri", redirectUriFromApp);
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token",
                    tokenRequest,
                    String.class
            );

            String accessToken = objectMapper.readTree(tokenResponse.getBody())
                    .get("access_token")
                    .asText();

            // 2. 사용자 정보 요청
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    userInfoRequest,
                    String.class
            );

            JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());
            String kakaoId = userInfo.get("id").asText();
            JsonNode emailNode = userInfo.get("kakao_account").get("email");
            String email = (emailNode != null && !emailNode.isNull()) ? emailNode.asText() : "no-email@kakao.com";
            String nickname = userInfo.get("properties").get("nickname").asText();

            // 3. JWT 생성
            String jwt = userService.loginWithKakao(kakaoId, email, nickname);

            // 4. 리디렉션
            String encodedToken = URLEncoder.encode(jwt, StandardCharsets.UTF_8);
            response.sendRedirect(frontendRedirectUri + "?token=" + encodedToken);

        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 전체 예외 로그 출력
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("카카오 로그인 중 오류 발생: " + e.getMessage());
        }
    }

}
