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
    private String profileImageUrl;
    private String refreshToken;

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
