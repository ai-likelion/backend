package com.likelion.ai_teacher_a.domain.userJr.repository;

import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserJrRepository extends JpaRepository<UserJr, Long> {
    List<UserJr> findByParentId(Long parentId);
}

