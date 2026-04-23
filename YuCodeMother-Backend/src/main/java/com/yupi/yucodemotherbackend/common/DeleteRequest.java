package com.yupi.yucodemotherbackend.common;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用的 删除请求包装类，后续删除请求直接继承该请求
 */
@Data
public class DeleteRequest implements Serializable {

	/**
	 * id
	 */
	private Long id;

	private static final long serialVersionUID = 1L;
}
