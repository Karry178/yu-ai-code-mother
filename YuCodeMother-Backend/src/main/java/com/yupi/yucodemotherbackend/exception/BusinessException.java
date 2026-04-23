package com.yupi.yucodemotherbackend.exception;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	/**
	 * 错误码
	 */
	private final int code;

	/**
	 * 错误码 对应的几种自定义构造函数
	 */
	public BusinessException (int code, String message) {
		super(message); // message直接调用父类方法
		this.code = code;
	}

	public BusinessException (ErrorCode errorCode) {
		this.code = errorCode.getCode();
	}

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.code = errorCode.getCode();
	}
}
