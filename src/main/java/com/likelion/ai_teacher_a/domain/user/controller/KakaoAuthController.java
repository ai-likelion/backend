package com.likelion.ai_teacher_a.domain.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.ai_teacher_a.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth/kakao")
public class KakaoAuthController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUriFromApp;

    @Value("${frontend.redirect-uri}")
    private String frontendRedirectUri; // e.g., http://localhost:3000/login/success

    /* 카카오 로그인 URL로 리디렉트 */
    @GetMapping("/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String redirectUri = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUriFromApp
                + "&response_type=code";
        response.sendRedirect(redirectUri);
    }

    /* 카카오 로그인 콜백 - JWT 발급 및 프론트엔드 리디렉션 */
    @GetMapping("/callback")
    public void kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        // 1. 토큰 요청
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

        // 2. 액세스 토큰 추출
        String accessToken = objectMapper.readTree(tokenResponse.getBody())
                .get("access_token")
                .asText();

        // 3. 사용자 정보 요청
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
        String email = userInfo.get("kakao_account").get("email").asText();
        String nickname = userInfo.get("properties").get("nickname").asText();

        // 4. 사용자 로그인 처리 및 JWT 발급
        String jwt = userService.loginWithKakao(kakaoId, email, nickname);

        // ✅ 5. 프론트엔드로 Redirect + JWT 전달 (URL에 토큰 포함)
        response.sendRedirect(frontendRedirectUri + "?token=" + jwt);
    }
}
