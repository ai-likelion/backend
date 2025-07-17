package com.likelion.ai_teacher_a.domain.userJr.repository;

import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserJrRepository extends JpaRepository<UserJr, Long> {
    List<UserJr> findByParentId(Long parentId);

    // ✅ 중복 체크 메서드 추가
    boolean existsByParentIdAndName(Long parentId, String name);


}