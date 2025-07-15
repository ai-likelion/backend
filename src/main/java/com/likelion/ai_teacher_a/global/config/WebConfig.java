package com.likelion.ai_teacher_a.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {
	private final String FRONT_URL;

	public WebConfig(@Value("${FRONT_URL:http://localhost:3000}") String frontUrl) {
		this.FRONT_URL = frontUrl;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		log.info("==== FRONT URL: {}==== ", FRONT_URL);
		registry.addMapping("/**")
			.allowedOrigins(
				FRONT_URL,
				"http://localhost:3000"
			)
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(true)
			.maxAge(3600);
	}
}