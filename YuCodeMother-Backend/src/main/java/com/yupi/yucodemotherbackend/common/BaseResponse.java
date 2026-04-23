package com.yupi.yucodemotherbackend.common;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

	private int code;

	private T data;

	private String message;

	/**
	 * 构造函数
	 */
	public BaseResponse(int code, T data, String message) {
		this.code = code;
		this.data = data;
		this.message = message;
	}

	public BaseResponse(int code, T data) {
		this.code = code;
		this.data = data;
	}

	public BaseResponse(ErrorCode errorCode) {
		this(errorCode.getCode(), null, errorCode.getMessage());
	}
}
