package com.likelion.ai_teacher_a.domain.user.entity;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")  // ← 이름 변경
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String phone;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

