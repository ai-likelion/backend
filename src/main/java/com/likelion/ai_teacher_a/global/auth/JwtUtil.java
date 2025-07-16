package com.likelion.ai_teacher_a.global.auth;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private final Key key;
	private final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60; // 1시간
	private final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

	public JwtUtil() {
		this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	}

	public long getAccessTokenExpirationTime() {
		return ACCESS_TOKEN_EXPIRATION_TIME;
	}

	public String createToken(Long userId) {
		return Jwts.builder()
			.setSubject(String.valueOf(userId))
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
			.signWith(key)
			.compact();
	}

	public String createRefreshToken(Long userId) {
		return Jwts.builder()
			.setSubject(String.valueOf(userId))
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
			.signWith(key)
			.compact();
	}

	public Long extractUserId(String token) {
		return Long.parseLong(
			Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject()
		);
	}

	public boolean validateToken(String token, CustomUserDetails userDetails) {
		Long userId = extractUserId(token);
		return (userId.equals(userDetails.getId()) && isValidateRefreshToken(token));
	}

	public boolean isValidateRefreshToken(String token) {
		Date expiration = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getExpiration();
		return !expiration.before(new Date());
	}
}