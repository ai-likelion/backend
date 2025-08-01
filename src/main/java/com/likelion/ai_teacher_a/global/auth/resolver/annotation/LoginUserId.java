package com.likelion.ai_teacher_a.global.auth.resolver.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Parameter(
	name = "Authorization",
	in = ParameterIn.HEADER,
	description = "Bearer {Access 토큰}",
	schema= @Schema(type="string"),
	required = true
)
public @interface LoginUserId {
}
