package com.likelion.ai_teacher_a.domain.logsolve.repository;

import com.likelion.ai_teacher_a.domain.logsolve.entity.LogSolve;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogSolveRepository extends JpaRepository<LogSolve, Long> {
    Page<LogSolve> findAllByUser(Pageable pageable, User user);

    long countByUser(User user);


    Page<LogSolve> findAllByUserJr(Pageable pageable, UserJr userJr);

    @Query("SELECT l FROM LogSolve l WHERE l.userJr = :userJr")
    List<LogSolve> findAllByUserJr(@Param("userJr") UserJr userJr);


}

