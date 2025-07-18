package com.likelion.ai_teacher_a.domain.userJr.repository;

import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserJrRepository extends JpaRepository<UserJr, Long> {

    @Query("SELECT uj FROM UserJr uj JOIN FETCH uj.user WHERE uj.user.id = :userId")
    List<UserJr> findByUserIdFetchJoin(@Param("userId") Long userId);


    boolean existsByUserIdAndNickname(Long userId, String nickname);


    void deleteAllByUser(User user);
}