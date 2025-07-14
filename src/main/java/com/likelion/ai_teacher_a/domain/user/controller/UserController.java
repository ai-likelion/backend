package com.likelion.ai_teacher_a.domain.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.ai_teacher_a.domain.user.dto.UserRequestDto;
import com.likelion.ai_teacher_a.domain.user.dto.UserResponseDto;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ 수동 등록 또는 Bean 주입도 가능
    private final RestTemplate restTemplate = new RestTemplate(); // ✅ 수동 등록 또는 Bean 주입도 가능

    @Value("${kakao.client-id}") // ✅ application.yml 또는 .properties에서 주입
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUriFromApp;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> create(@RequestBody UserRequestDto dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable Long id,
            @RequestBody UserRequestDto dto
    ) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @PatchMapping("/{userId}/profile-image")
    public ResponseEntity<Void> setProfileImage(
            @PathVariable Long userId,
            @RequestParam Long imageId) {
        userService.setProfileImage(userId, imageId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/oauth/kakao/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String redirectUri = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUriFromApp
                + "&response_type=code";
        response.sendRedirect(redirectUri);
    }

    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code) throws IOException {
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

        String accessToken = objectMapper.readTree(tokenResponse.getBody()).get("access_token").asText();

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

        String jwt = userService.loginWithKakao(kakaoId, email, nickname);

        // Redirect가 아닌 API 테스트용으로 토큰 반환
        return ResponseEntity.ok(jwt);
    }

    @GetMapping("/login")
    public void redirectToKakaoLogin(HttpServletResponse response) throws IOException {
        String redirectUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUriFromApp
                + "&response_type=code";
        response.sendRedirect(redirectUrl);
    }
}
