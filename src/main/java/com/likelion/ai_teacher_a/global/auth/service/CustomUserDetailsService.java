package com.likelion.ai_teacher_a.global.auth.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.global.auth.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public CustomUserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		Long id = Long.parseLong(userId);
		User user = userRepository.findById(id)
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
		return new CustomUserDetails(user);
	}
}
