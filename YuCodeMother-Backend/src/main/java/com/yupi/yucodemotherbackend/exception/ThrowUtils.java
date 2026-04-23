package com.yupi.yucodemotherbackend.exception;

public class ThrowUtils {

	/**
	 * 条件成立则抛异常
	 * @param condition 条件
	 * @param runtimeException 系统异常
	 */
	public static void throwIf(boolean condition, RuntimeException runtimeException) {
		if (condition) {
			throw runtimeException;
		}
	}


	/**
	 * 条件成立则抛异常
	 * @param condition 条件
	 * @param errorCode 自定义状态码
	 */
	public static void throwIf(boolean condition, ErrorCode errorCode) {
		// 调用throwIf方法，传入condition 和 BusinessException下的errorCode表示的内容
		throwIf(condition, new BusinessException(errorCode));
	}


	/**
	 * 条件成立则抛异常与异常消息
	 * @param condition 条件
	 * @param errorCode 异常类
	 * @param message 异常消息
	 */
	public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
		throwIf(condition, new BusinessException(errorCode, message));
	}
}
