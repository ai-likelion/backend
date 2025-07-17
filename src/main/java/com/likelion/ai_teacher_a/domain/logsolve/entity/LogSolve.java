package com.likelion.ai_teacher_a.domain.logsolve.entity;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}