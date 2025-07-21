package com.likelion.ai_teacher_a.global.auth.handler;

import static com.likelion.ai_teacher_a.global.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository.*;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.global.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.likelion.ai_teacher_a.global.auth.util.CookieUtils;
import com.likelion.ai_teacher_a.global.auth.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		String targetUrl = determineTargetUrl(request, response, authentication);
		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		Map<String, Object> attributes = oAuth2User.getAttributes();
		Long kakaoId = (Long)attributes.get("id");

		User user = userRepository.findById(kakaoId)
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
		log.info("==== 로그인 요청 성공 ====");
		log.info("targetUrl= "+ targetUrl);
		log.info("kakaoId= " + kakaoId);
		log.info(user.getName());
		String accessToken = jwtUtil.createToken(user.getId());
		String refreshToken = jwtUtil.createRefreshToken(user.getId());

		long expiresIn = new Date().getTime() + jwtUtil.getAccessTokenExpirationTime();

		user.setRefreshToken(refreshToken);
		userRepository.save(user);
		response.addCookie(CookieUtils.createRefeshTokenCookie(refreshToken));

		String finalUrl = UriComponentsBuilder.fromUriString(targetUrl)
			.queryParam("accessToken", accessToken)
			.queryParam("accessTokenExpiresIn", expiresIn)
			.build().toUriString();

		clearAuthenticationAttributes(request, response);
		getRedirectStrategy().sendRedirect(request, response, finalUrl);
	}

	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) {
		Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
			.map(Cookie::getValue);

		String targetUrl = redirectUri.orElse("http://localhost:3000/success");

		return UriComponentsBuilder.fromUriString(targetUrl)
			.build().toUriString();
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}
}
