package com.yupi.yucodemotherbackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.yupi.yucodemotherbackend.annotation.AuthCheck;
import com.yupi.yucodemotherbackend.common.BaseResponse;
import com.yupi.yucodemotherbackend.common.ResultUtils;
import com.yupi.yucodemotherbackend.constatnt.UserConstant;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yupi.yucodemotherbackend.model.entity.ChatHistory;
import com.yupi.yucodemotherbackend.service.ChatHistoryService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

	@Resource
	private ChatHistoryService chatHistoryService;

	@Resource
	private UserService userService;


	/**
	 * 分页查询某个应用的对话历史（游标查询）
	 * @param appId 应用Id
	 * @param pageSize 页面大小
	 * @param lastCreateTime （即游标）最后一条记录的创建时间，默认无游标
	 * @param request 登录请求
	 * @return 对话历史分页
	 */
	@GetMapping("/app/{appId}")
	public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
	                                                          @RequestParam(defaultValue = "10") int pageSize,
	                                                          @RequestParam(required = false) LocalDateTime lastCreateTime,
	                                                          HttpServletRequest request) {
		// 获取登录用户
		User loginUser = userService.getLoginUser(request);
		// 调用对话历史分页方法
		Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
		return ResultUtils.success(result);
	}


	/**
	 * 管理员分页查询所有对话历史
	 *
	 * @param chatHistoryQueryRequest 对话历史查询请求
	 * @return 对话历史分页
	 */
	@PostMapping("/admin/list/page/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
		ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
		long pageNum = chatHistoryQueryRequest.getPageNum();
		long pageSize = chatHistoryQueryRequest.getPageSize();
		// 查询数据
		QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
		Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
		return ResultUtils.success(result);
	}
}
