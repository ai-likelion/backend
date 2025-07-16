package com.likelion.ai_teacher_a.domain.user.entity;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long userId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;
    private String password;
    private String provider; // "KAKAO" 또는 "LOCAL"
    private String phone;
    private String kakaoId;

    @OneToOne
    private Image profileImage;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
