package com.likelion.ai_teacher_a.global.auth.service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		String userNameAttributeName =
			userRequest.getClientRegistration()
				.getProviderDetails()
				.getUserInfoEndpoint()
				.getUserNameAttributeName();

		Map<String, Object> attributes = oAuth2User.getAttributes();

		Long kakaoId = (Long)attributes.get("id");

		Map<String, Object> properties = (Map<String, Object>)attributes.get("properties");
		String nickname = (String)properties.get("nickname");
		String email = (String) ((Map<String, Object>) attributes.get("kakao_account")).get("email");

		Optional<User> userOptional = userRepository.findById(kakaoId);
		User user;
		if (userOptional.isEmpty()) {
			user = User.builder()
				.id(kakaoId)
				.name(nickname)
				.email(email)
				.provider("kakao")
				.build();
			userRepository.save(user);
		}

		return new DefaultOAuth2User(
			Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
			attributes,
			userNameAttributeName
		);
	}
}

