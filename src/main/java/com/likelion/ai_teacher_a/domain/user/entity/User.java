package com.likelion.ai_teacher_a.domain.user.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@Column(nullable = false)
	private Long id;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	private String email;

	@Column(nullable = false)
	private String name;
	private String password;
	private String provider;
	private String phone;
	private String refreshToken;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
