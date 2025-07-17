package com.likelion.ai_teacher_a.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.likelion.ai_teacher_a.global.auth.filter.JwtAuthenticationFilter;
import com.likelion.ai_teacher_a.global.auth.handler.OAuth2AuthenticationSuccessHandler;
import com.likelion.ai_teacher_a.global.auth.service.CustomOAuth2UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final CorsConfigurationSource corsConfigurationSource;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(CsrfConfigurer<HttpSecurity>::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/",
					"/login.html",
					"/success.html",
					"/refresh_test.html",
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/login/oauth2/code/kakao",
					"/oauth2/authorization/kakao",
					"/api/**" //TODO: 추후 회원 완료 후 삭제 필요
				).permitAll()
				.anyRequest().authenticated()
			)
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService)
				)
				.successHandler(oAuth2AuthenticationSuccessHandler)
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					response.setContentType("application/json;charset=UTF-8");
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("{\"message\": \"" + authException.getMessage() + "\"}");
				})
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					response.setContentType("application/json;charset=UTF-8");
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.getWriter().write("{\"message\": \"" + accessDeniedException.getMessage() + "\"}");
				})
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
