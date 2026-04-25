package com.yupi.yucodemotherbackend.aop;

import com.yupi.yucodemotherbackend.annotation.AuthCheck;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.enums.UserRoleEnum;
import com.yupi.yucodemotherbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

	@Resource
	private UserService userService;

	/**
	 * 执行拦截 - 只识别有 @annotation(authCheck)注解 的方法，实现权限校验拦截的目的
	 *
	 * @param joinPoint 切入点
	 * @param authCheck 权限校验注解
	 * @return
	 */
	@Around("@annotation(authCheck)")
	public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {

		String mustRole = authCheck.mustRole();

		// 1.获取当前登录用户
		// 先从RequestContextHolder中拿到requestAttributes
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		// 然后从requestAttributes强转到HttpServletRequest
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
		User loginUser = userService.getLoginUser(request);
		// 2.获取登录用户必须拥有的枚举类 mustRole
		UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

		// 3.不需要权限，直接通过
		if (mustRoleEnum == null) {
			return joinPoint.proceed();
		}

		// 4.需要权限才能通过
		UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
		// 没有权限，直接拒绝
		ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.NO_AUTH_ERROR);
		// 要求必须有管理员权限，但当前登录用户没有管理员权限
		ThrowUtils.throwIf(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum), ErrorCode.NO_AUTH_ERROR);
		// 否则，直接通过普通用户的权限校验，放行
		return joinPoint.proceed();

	}
}
