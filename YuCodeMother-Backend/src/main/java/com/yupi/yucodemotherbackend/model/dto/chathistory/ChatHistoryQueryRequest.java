package com.yupi.yucodemotherbackend.model.dto.chathistory;

import com.yupi.yucodemotherbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

	/**
	 * id
	 */
	private Long id;

	/**
	 * 消息内容
	 */
	private String message;

	/**
	 * 消息类型（user/id）
	 */
	private String messageType;

	/**
	 * 应用Id
	 */
	private Long appId;

	/**
	 * 用户Id
	 */
	private Long userId;


	/**
	 * 游标查询 - 最后一条记录的创建时间
	 * 用于分页查询，获取早于此事件的记录
	 */
	private LocalDateTime lastCreateTime;

	private static final long serialVersionUID = 1L;
}
