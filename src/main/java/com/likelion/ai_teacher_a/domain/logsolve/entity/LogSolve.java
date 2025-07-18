package com.likelion.ai_teacher_a.domain.logsolve.entity;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "image_id")
    private Image image;


    @Column(columnDefinition = "TEXT")
    private String result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_jr_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserJr userJr;


}