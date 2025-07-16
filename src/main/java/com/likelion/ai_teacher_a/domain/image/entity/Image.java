package com.likelion.ai_teacher_a.domain.image.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private Integer fileSize;

    private String fileName;

    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    private ImageType type; // PROFILE, ETC


    private String url;

    @PrePersist
    public void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}