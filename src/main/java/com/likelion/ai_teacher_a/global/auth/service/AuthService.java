package com.likelion.ai_teacher_a.global.auth.service;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.global.auth.JwtUtil;
import com.likelion.ai_teacher_a.global.auth.service.dto.TokenRefreshResult;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;

	public TokenRefreshResult refreshAccessToken(String refreshToken) {
		if (isInvalidToken(refreshToken)) {
			return buildResponseForInvaildToken();
		}

		Long userId = jwtUtil.extractUserId(refreshToken);
		User user = getUser(userId);

		if (!user.getRefreshToken().equals(refreshToken)) {
			return buildResponseForInvaildToken();
		}

		String newAccessToken = jwtUtil.createToken(userId);
		String newRefreshToken = jwtUtil.createRefreshToken(userId);
		saveUserToken(user, newRefreshToken);

		Cookie refreshTokenCookie = createNewCookie(newRefreshToken);

		String expiresIn = getExpireDateTime();

		return buildResponseForOK(newAccessToken, expiresIn, refreshTokenCookie);
	}

	private TokenRefreshResult buildResponseForOK(String accessToken, String expireDateTime, Cookie cookie) {
		return TokenRefreshResult.builder()
			.httpStatus(HttpStatus.OK)
			.accessToken(accessToken)
			.expireDateTime(expireDateTime)
			.newCookie(cookie)
			.build();
	}

	private String getExpireDateTime() {
		return String.valueOf(new Date().getTime() + jwtUtil.getAccessTokenExpirationTime());
	}

	private void saveUserToken(User user, String newRefreshToken) {
		user.setRefreshToken(newRefreshToken);
		userRepository.save(user);
	}

	private Cookie createNewCookie(String newRefreshToken) {
		Cookie refreshTokenCookie = new Cookie("refresh_token", newRefreshToken);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setSecure(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);
		return refreshTokenCookie;
	}

	private TokenRefreshResult buildResponseForInvaildToken() {
		return TokenRefreshResult.builder()
			.httpStatus(HttpStatus.BAD_REQUEST)
			.message("유효하지 않은 Refresh Token 입니다.")
			.build();
	}

	private boolean isInvalidToken(String refreshToken) {
		return refreshToken == null || !jwtUtil.isValidateRefreshToken(refreshToken);
	}

	private User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
	}
}
