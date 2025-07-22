package com.likelion.ai_teacher_a.domain.userJr.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;

public interface UserJrRepository extends JpaRepository<UserJr, Long> {

	@Query("""
		SELECT uj FROM UserJr uj
		LEFT JOIN FETCH uj.user
		LEFT JOIN FETCH uj.image
		WHERE uj.user.id = :userId
		ORDER BY uj.userJrId ASC""")
	List<UserJr> findByUserIdFetchJoin(@Param("userId") Long userId);

	boolean existsByUserIdAndNickname(Long userId, String nickname);

	@Query("""
		    SELECT uj FROM UserJr uj
		    LEFT JOIN FETCH uj.user
		    LEFT JOIN FETCH uj.image
		    WHERE uj.userJrId = :userJrId
		""")
	Optional<UserJr> findByIdWithUserAndImage(@Param("userJrId") Long userJrId);

}