package com.likelion.ai_teacher_a.domain.userJr.entity;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@Table(name = "user_jr")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserJr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userJrId;

    private String nickname;

    @Min(1)
    @Max(6)
    private int schoolGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;



    @Builder
    public UserJr(String nickname, int schoolGrade, User user, Image image) {
        this.nickname = nickname;
        this.schoolGrade = schoolGrade;
        this.user = user;
        this.image = image;
    }

}