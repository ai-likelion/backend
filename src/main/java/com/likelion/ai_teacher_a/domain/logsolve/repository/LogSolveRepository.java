package com.likelion.ai_teacher_a.domain.logsolve.repository;

import com.likelion.ai_teacher_a.domain.logsolve.entity.LogSolve;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogSolveRepository extends JpaRepository<LogSolve, Long> {
    Page<LogSolve> findAllByUser(Pageable pageable, User user);

    long countByUser(User user);

}

