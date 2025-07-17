package com.likelion.ai_teacher_a.global.auth.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.likelion.ai_teacher_a.global.auth.CustomUserDetails;
import com.likelion.ai_teacher_a.global.auth.util.JwtUtil;
import com.likelion.ai_teacher_a.global.auth.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain)
		throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			Long userId = jwtUtil.extractUserId(token);

			if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				CustomUserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
				if (jwtUtil.validateToken(token, userDetails)) {
					UsernamePasswordAuthenticationToken auth =
						new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities()
						);
					auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			}
		}

		filterChain.doFilter(request, response);
	}
}