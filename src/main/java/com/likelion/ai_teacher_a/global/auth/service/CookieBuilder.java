package com.likelion.ai_teacher_a.global.auth.service;

import jakarta.servlet.http.Cookie;

public class CookieBuilder {
	public static Cookie createRefeshTokenCookie(String newRefreshToken) {
		Cookie refreshTokenCookie = new Cookie("refresh_token", newRefreshToken);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setSecure(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setDomain("ai-teacher-back-latest.onrender.com");
		refreshTokenCookie.setAttribute("SameSite", "None");
		refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);
		return refreshTokenCookie;
	}
}
