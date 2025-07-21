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
import java.util.Optional;

@Repository
public interface LogSolveRepository extends JpaRepository<LogSolve, Long> {
    Page<LogSolve> findAllByUser(Pageable pageable, User user);

    long countByUser(User user);

    @Query(
            value = """
                    SELECT l FROM LogSolve l
                    LEFT JOIN FETCH l.image i
                    LEFT JOIN FETCH l.userJr uj
                    LEFT JOIN FETCH uj.image
                    WHERE l.userJr = :userJr
                    """,
            countQuery = "SELECT COUNT(l) FROM LogSolve l WHERE l.userJr = :userJr"
    )
    Page<LogSolve> findPageByUserJrWithImage(Pageable pageable, @Param("userJr") UserJr userJr);


    @Query("SELECT l FROM LogSolve l LEFT JOIN FETCH l.image WHERE l.userJr = :userJr")
    List<LogSolve> findAllByUserJrWithImage(@Param("userJr") UserJr userJr);


    @Query("""
                SELECT l FROM LogSolve l
                LEFT JOIN FETCH l.image
                LEFT JOIN FETCH l.user
                WHERE l.id = :logSolveId
            """)
    Optional<LogSolve> findByIdWithImageAndUser(@Param("logSolveId") Long logSolveId);


}

