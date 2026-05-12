package com.yupi.yucodemotherbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yupi.yucodemotherbackend.constatnt.UserConstant;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yupi.yucodemotherbackend.model.entity.App;
import com.yupi.yucodemotherbackend.model.entity.ChatHistory;
import com.yupi.yucodemotherbackend.mapper.ChatHistoryMapper;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.enums.ChatHistoryMessageTypeEnum;
import com.yupi.yucodemotherbackend.service.AppService;
import com.yupi.yucodemotherbackend.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

	// 引入 AppService，分页功能会用到
	// 使用 @Lazy 解决循环依赖问题
	@Resource
	@Lazy
	private AppService appService;

	/**
	 * 添加对话历史
	 *
	 * @param appId       应用Id
	 * @param message     聊天消息
	 * @param messageType 消息类型
	 * @param userId      用户Id
	 * @return 是否添加成功
	 */
	@Override
	public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {

		// 1.参数校验
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用Id不能为空");
		ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "输入信息不能为空");
		ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
		ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户Id不能为空");
		// 2.验证消息类型是否有效
		ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
		ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型");

		// 3.插入数据库：
		// 以前一直使用 构造对象后，用allset方法挨个设置值
		// ☆ 现在可以使用MyBatis-Flux的特性：打builder注解，使用构造器模式快速构建对象。如ChatHistory.builder().build();
		ChatHistory chatHistory = ChatHistory.builder()
				.appId(appId)
				.message(message)
				.messageType(messageType)
				.userId(userId)
				.build();
		return this.save(chatHistory);
	}


	/**
	 * 根据应用 id 删除对话历史
	 *
	 * @param appId 应用id
	 * @return 是否删除成功
	 */
	@Override
	public boolean deleteByAppId(Long appId) {
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用Id不能为空");
		QueryWrapper queryWrapper = QueryWrapper.create()
				.eq("appId", appId);
		return this.remove(queryWrapper);
	}


	/**
	 * 分页查询某 APP 的对话记录
	 *
	 * @param appId 应用Id
	 * @param pageSize 每页最大记录量
	 * @param lastCreateTime 最后创建时间 [游标]
	 * @param loginUser 登录用户
	 * @return
	 */
	@Override
	public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
	                                                  LocalDateTime lastCreateTime,
	                                                  User loginUser) {
		// 1.校验参数
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用Id不能为空");
		ThrowUtils.throwIf(pageSize <= 0 || pageSize >= 50, ErrorCode.PARAMS_ERROR, "页面大小要控制在 1-50 之间");
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
		// 2.权限校验：只有应用创建者和管理员可以查看
		App app = appService.getById(appId);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
		boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
			// 验证是否为app的创建者
		boolean isCreator = app.getUserId().equals(loginUser.getId());
		ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
		// 3.构建查询条件
		ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
		queryRequest.setAppId(appId);
		queryRequest.setLastCreateTime(lastCreateTime);
			// 调用Service层的getQueryWrapper()方法
		QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
			// 调用分页查询
		return this.page(Page.of(1, pageSize), queryWrapper);
	}


	/**
	 * 加载对话记忆到内存
	 * @param appId 应用Id
	 * @param chatMemory 对话历史
	 * @param maxCount 消息最大条数
	 * @return 加载成功条数
	 */
	@Override
	public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
		try {
			// 1.构造查询方法：查询最新的某一个 appId 对应的对话记忆
			QueryWrapper queryWrapper = QueryWrapper.create()
					// 方法引用：直接从ChatHistory中拿参数
					.eq(ChatHistory::getAppId, appId)
					.orderBy(ChatHistory::getCreateTime, false)
					// 如果offset写0不写1，会调用两次最新的重复消息，这一步需要重点解释！
					.limit(1, maxCount);
			// 2.获取对话记忆列表
			List<ChatHistory> historyList = this.list(queryWrapper);
			if (CollUtil.isEmpty(historyList)) {
				return 0;
			}
			// 将历史消息列表反转，确保按照时间正序，类似于微信聊天记录
			historyList = historyList.reversed();
			// 3.按照时间顺序将消息添加到记忆中
			int loadedCount = 0;
			// 先清理历史缓存，防止重复加载
			chatMemory.clear();
			// 循环遍历获取历史消息
			for (ChatHistory history : historyList) {
				if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
					// 从历史消息中拿到用户发送的消息，然后添加到对话记忆中
					chatMemory.add(UserMessage.from(history.getMessage()));
				} else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
					chatMemory.add(AiMessage.from(history.getMessage()));
				}
				// 每一次都计数+1
				loadedCount++;
			}
			log.info("成功为 appId：{} 加载了 {} 条历史消息", appId, loadedCount);
			return loadedCount;
		} catch (Exception e) {
			log.error("加载历史对话失败，appId：{}，error：{}", appId, e.getMessage(), e);
			// 加载失败不影响系统运行，只是没有历史上下文
			return 0;
		}
	}


	/**
	 * 获取查询条件
	 *
	 * @param chatHistoryQueryRequest 查询聊天历史请求
	 * @return
	 */
	@Override
	public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
		QueryWrapper queryWrapper = QueryWrapper.create();
		if (chatHistoryQueryRequest == null) {
			return queryWrapper;
		}
		// 1.获取chatHistoryQueryRequest的参数值
		Long id = chatHistoryQueryRequest.getId();
		String message = chatHistoryQueryRequest.getMessage();
		String messageType = chatHistoryQueryRequest.getMessageType();
		Long appId = chatHistoryQueryRequest.getAppId();
		Long userId = chatHistoryQueryRequest.getUserId();
		LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
		String sortField = chatHistoryQueryRequest.getSortField(); // 排序字段
		String sortOrder = chatHistoryQueryRequest.getSortOrder(); // 排序顺序

		// 2.拼接查询条件
		queryWrapper.eq("id", id)
				.like("message", message)
				.eq("messageType", messageType)
				.eq("appId", appId)
				.eq("userId", userId);
		// 3.【重点】游标查询逻辑 - 只使用 createTime 作为游标
		if (lastCreateTime != null) {
			// 如果游标存在，则 lt 查询小于 lastCreateTime(游标) 的数据，lt是less than查询
			queryWrapper.lt("createTime", lastCreateTime);
		}
		// 排序
		if (StrUtil.isNotBlank(sortField)) {
			// 如果排序字段存在，按排序字段升序排列
			queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
		} else {
			// 否则默认按创建时间降序排列
			queryWrapper.orderBy("createTime", false);
		}
		return queryWrapper;
	}

}
