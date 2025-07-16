package com.likelion.ai_teacher_a.global.auth.handler;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.global.auth.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final String frontendRedirectUri;

	public OAuth2AuthenticationSuccessHandler(
		JwtUtil jwtUtil,
		UserRepository userRepository,
		@Value("${frontend.redirect-uri}") String frontendRedirectUri
	) {
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
		this.frontendRedirectUri = frontendRedirectUri;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		Long kakaoId = (Long)attributes.get("id");

		User user = userRepository.findById(kakaoId)
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

		String accessToken = jwtUtil.createToken(user.getId());
		String refreshToken = jwtUtil.createRefreshToken(user.getId());

		long expiresIn = new Date().getTime() + jwtUtil.getAccessTokenExpirationTime();

		user.setRefreshToken(refreshToken);
		userRepository.save(user);

		Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setSecure(true); // HTTPS 환경에서만 전송
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 7일
		response.addCookie(refreshTokenCookie);

		String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
			.queryParam("accessToken", accessToken)
			.queryParam("accessTokenExpiresIn", expiresIn)
			.build().toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
