package com.yupi.yucodemotherbackend.exception;

import com.yupi.yucodemotherbackend.common.BaseResponse;
import com.yupi.yucodemotherbackend.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常响应类 - AOP切面编程
 *
 * 注意！由于本项目使用的 Spring Boot 版本>= 3.4、并且是OpenAPI 3 版本的 Knife4j，
 * 这会导致@RestcontrollerAdvice注解不兼容，所以必须给这个类加上 @Hidden 注解，不被 Swagger 加载。
 * 虽然网上也有其他的解决方案，但这种方法是最直接有效的。
 */
@Hidden
@RestControllerAdvice  // 目的是捕获Controller中的所有异常
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public BaseResponse<?> businessExceptionHandler(BusinessException e) {
		log.error("BusinessException", e);
		// 如 code 40000  data: null  message:用户未登录
		return ResultUtils.error(e.getCode(), e.getMessage());
	}


	@ExceptionHandler(RuntimeException.class)
	public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
		log.error("RuntimeException", e);
		//
		return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
	}
}
