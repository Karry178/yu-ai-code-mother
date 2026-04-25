package com.yupi.yucodemotherbackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 生效目标 - 给方法打注解
@Retention(RetentionPolicy.RUNTIME) // 一般情况下都是：运行时生效注解
public @interface AuthCheck {

	/**
	 * 必须有某个角色
	 *
	 * @return
	 */
	String mustRole() default "";
}
