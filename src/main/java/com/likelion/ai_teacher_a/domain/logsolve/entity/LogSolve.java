package com.likelion.ai_teacher_a.domain.logsolve.entity;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "log_solve")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class LogSolve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logSolveId;

    @OneToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(columnDefinition = "TEXT")
    private String result;
}