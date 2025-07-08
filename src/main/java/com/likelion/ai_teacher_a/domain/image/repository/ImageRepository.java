package com.likelion.ai_teacher_a.domain.image.repository;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}

