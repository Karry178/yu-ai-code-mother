package com.yupi.yucodemotherbackend.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yupi.yucodemotherbackend.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yupi.yucodemotherbackend.model.entity.ChatHistory;
import com.yupi.yucodemotherbackend.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

	/**
	 * 添加对话历史
	 *
	 * @param appId       应用Id
	 * @param message     聊天消息
	 * @param messageType 消息类型
	 * @param userId      用户Id
	 * @return 是否添加成功
	 */
	boolean addChatMessage(Long appId, String message, String messageType, Long userId);


	/**
	 * 根据应用 id 删除对话历史
	 *
	 * @param appId 应用id
	 * @return 是否删除成功
	 */
	boolean deleteByAppId(Long appId);


	/**
	 * 分页查询某 APP 的对话记录
	 *
	 * @param appId 应用Id
	 * @param pageSize 每页最大记录量
	 * @param lastCreateTime 最后创建时间 [游标]
	 * @param loginUser 登录用户
	 * @return
	 */
	Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
	                                           LocalDateTime lastCreateTime,
	                                           User loginUser);


	/**
	 * 加载对话记忆到内存
	 * @param appId 应用Id
	 * @param chatMemory 对话历史
	 * @param maxCount 消息最大条数
	 * @return 加载成功条数
	 */
	int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

	/**
	 * 获取查询条件
	 *
	 * @param chatHistoryQueryRequest 查询聊天历史请求
	 * @return
	 */
	QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
