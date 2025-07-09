package com.likelion.ai_teacher_a.domain.image.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor  // <- 이거도 필요합니다!
@Builder              // ✅ 이게 꼭 필요합니다!
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private Integer fileSize;

    private String fileName;

    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    private ImageType type; // PROFILE, ETC

    @Lob
    private byte[] data;

    @PrePersist
    public void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}