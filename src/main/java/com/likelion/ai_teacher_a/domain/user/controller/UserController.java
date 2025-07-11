//package com.likelion.ai_teacher_a.domain.user.controller;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.likelion.ai_teacher_a.domain.user.dto.UserRequestDto;
//import com.likelion.ai_teacher_a.domain.user.dto.UserResponseDto;
//import com.likelion.ai_teacher_a.domain.user.service.UserService;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.*;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//
//@RestController
//@RequestMapping("/api/users")
//@RequiredArgsConstructor
//public class UserController {
//
//    private final UserService userService;
//
//    @PostMapping("/signup")
//    public ResponseEntity<UserResponseDto> create(@RequestBody UserRequestDto dto) {
//        return ResponseEntity.ok(userService.createUser(dto));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<UserResponseDto> get(@PathVariable Long id) {
//        return ResponseEntity.ok(userService.getUser(id));
//    }
//
//    @PatchMapping("/{id}")
//    public ResponseEntity<UserResponseDto> update(
//            @PathVariable Long id,
//            @RequestBody UserRequestDto dto
//    ) {
//        return ResponseEntity.ok(userService.updateUser(id, dto));
//    }
//
//    @PatchMapping("/{userId}/profile-image")
//    public ResponseEntity<Void> setProfileImage(
//            @PathVariable Long userId,
//            @RequestParam Long imageId) {
//        userService.setProfileImage(userId, imageId);
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        userService.deleteUser(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/oauth/kakao/login")
//    public void kakaoLogin(HttpServletResponse response) throws IOException {
//        String redirectUri = "https://kauth.kakao.com/oauth/authorize"
//                + "?client_id=" + clientId
//                + "&redirect_uri=" + redirectUriFromApp
//                + "&response_type=code";
//        response.sendRedirect(redirectUri);
//    }
//
//    @GetMapping("/oauth/kakao/callback")
//    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code) throws IOException {
//        // 1. 토큰 요청
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("grant_type", "authorization_code");
//        params.add("client_id", clientId);
//        params.add("redirect_uri", redirectUriFromApp);
//        params.add("code", code);
//
//        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
//
//        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(
//                "https://kauth.kakao.com/oauth/token",
//                tokenRequest,
//                String.class
//        );
//
//        // 2. 액세스 토큰 추출
//        String accessToken = objectMapper.readTree(tokenResponse.getBody()).get("access_token").asText();
//
//        // 3. 사용자 정보 요청
//        HttpHeaders userInfoHeaders = new HttpHeaders();
//        userInfoHeaders.setBearerAuth(accessToken);
//        HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);
//
//        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
//                "https://kapi.kakao.com/v2/user/me",
//                HttpMethod.GET,
//                userInfoRequest,
//                String.class
//        );
//
//        JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());
//        String kakaoId = userInfo.get("id").asText();
//        String email = userInfo.get("kakao_account").get("email").asText();
//        String nickname = userInfo.get("properties").get("nickname").asText();
//
//        // 4. 우리 DB에 사용자 등록 or 로그인 처리
//        userService.loginWithKakao(kakaoId, email, nickname);
//
//        return ResponseEntity.ok("로그인 성공");
//    }
//
//    @GetMapping("/login")
//    public void redirectToKakaoLogin(HttpServletResponse response) throws IOException {
//        String redirectUrl = "https://kauth.kakao.com/oauth/authorize"
//                + "?client_id=" + clientId
//                + "&redirect_uri=" + redirectUri
//                + "&response_type=code";
//        response.sendRedirect(redirectUrl);
//    }
//
//
//}
//
