package com.likelion.ai_teacher_a.domain.logsolve.repository;

import com.likelion.ai_teacher_a.domain.logsolve.entity.LogSolve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogSolveRepository extends JpaRepository<LogSolve, Long> {
}
