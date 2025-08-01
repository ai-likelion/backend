package com.likelion.ai_teacher_a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AiTeacherAApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiTeacherAApplication.class, args);
	}

}
