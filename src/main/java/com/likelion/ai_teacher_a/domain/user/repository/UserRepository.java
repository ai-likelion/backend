package com.likelion.ai_teacher_a.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.likelion.ai_teacher_a.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	@Modifying
	@Query(value = """
		DELETE FROM log_solve WHERE user_id = :userId;
		DELETE FROM user_jr WHERE user_id = :userId;
		DELETE FROM image WHERE user_id = :userId;
		DELETE FROM users WHERE id = :userId;
		""", nativeQuery = true)
	void deleteById(Long userId);
}