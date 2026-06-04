package com.yupi.yucodemotherbackend.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式信息响应基类
 */
@Data
@AllArgsConstructor // 拥有全部参数的注解
@NoArgsConstructor // 无参构造函数
public class StreamMessage {

	/**
	 * 消息类型
	 */
	private String type;

}
