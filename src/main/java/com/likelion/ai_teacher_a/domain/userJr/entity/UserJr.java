package com.likelion.ai_teacher_a.domain.userJr.entity;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_jr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserJr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userJrId;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;

    private String name;

    private String schoolGrade;

    @OneToOne
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;
}
