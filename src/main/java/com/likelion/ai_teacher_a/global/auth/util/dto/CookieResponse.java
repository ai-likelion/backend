package com.likelion.ai_teacher_a.global.auth.util.dto;

import jakarta.servlet.http.Cookie;
import lombok.Builder;

@Builder
public record CookieResponse(
	Cookie cookie
) {
}
