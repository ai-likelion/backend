package com.likelion.ai_teacher_a.domain.user.service;

import org.springframework.stereotype.Service;

import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import com.likelion.ai_teacher_a.domain.logsolve.repository.LogSolveRepository;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.domain.userJr.repository.UserJrRepository;
import com.likelion.ai_teacher_a.global.auth.util.CookieUtils;
import com.likelion.ai_teacher_a.global.auth.util.JwtUtil;
import com.likelion.ai_teacher_a.global.auth.util.dto.CookieResponse;
import com.likelion.ai_teacher_a.global.exception.CustomException;
import com.likelion.ai_teacher_a.global.exception.ErrorCode;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final ImageRepository imageRepository;
	private final UserJrRepository userJrRepository;
	private final LogSolveRepository logSolveRepository;
	private final JwtUtil jwtUtil;

	@Transactional
	public void deleteUserById(Long id, String refreshToken) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		deleteRefreshToken(id, refreshToken);
		userRepository.deleteById(id);
	}

	@Transactional
	public CookieResponse deleteRefreshToken(Long userId, String refreshToken) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		user.setRefreshToken(null);
		Cookie cookie = CookieUtils.deleteRefeshTokenCookie(refreshToken);
		return CookieResponse.builder().cookie(cookie).build();
	}
}

