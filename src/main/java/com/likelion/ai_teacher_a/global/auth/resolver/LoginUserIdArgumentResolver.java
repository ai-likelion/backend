package com.likelion.ai_teacher_a.global.auth.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.likelion.ai_teacher_a.global.auth.CustomUserDetails;
import com.likelion.ai_teacher_a.global.auth.resolver.annotation.LoginUserId;

@Component
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUserId.class)
			&& Long.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory) throws Exception {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null) {
			throw new RuntimeException("인증 정보가 없습니다.");
		}

		Object principal = auth.getPrincipal();
		if (principal instanceof CustomUserDetails) {
			return ((CustomUserDetails)principal).getId();
		} else if (principal instanceof OAuth2User) {
			Object idAttr = ((OAuth2User)principal).getAttributes().get("id");
			if (idAttr == null) {
				throw new RuntimeException("OAuth2User 에 id 속성이 없습니다.");
			}
			return Long.valueOf(idAttr.toString());
		} else {
			throw new RuntimeException("지원하지 않는 로그인 방식입니다.");
		}
	}
}

