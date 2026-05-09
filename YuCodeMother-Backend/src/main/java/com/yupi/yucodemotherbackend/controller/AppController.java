package com.yupi.yucodemotherbackend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import cn.hutool.json.JSONUtil;
import com.yupi.yucodemotherbackend.model.dto.app.*;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.yupi.yucodemotherbackend.annotation.AuthCheck;
import com.yupi.yucodemotherbackend.common.BaseResponse;
import com.yupi.yucodemotherbackend.common.DeleteRequest;
import com.yupi.yucodemotherbackend.common.ResultUtils;
import com.yupi.yucodemotherbackend.constatnt.AppConstant;
import com.yupi.yucodemotherbackend.constatnt.UserConstant;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.model.entity.App;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import com.yupi.yucodemotherbackend.model.vo.AppVO;
import com.yupi.yucodemotherbackend.service.AppService;
import com.yupi.yucodemotherbackend.service.UserService;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 控制层。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
@Slf4j
@RestController
@RequestMapping("/app")
public class AppController {

	// 注入AppService
	@Resource
	private AppService appService;

	// 注入UserService
	@Resource
	private UserService userService;


	/**
	 * 【重点】与模型对话生成代码（SSE流式返回）
	 *
	 * @param appId 应用ID
	 * @param message 提示词
	 * @param request 登录请求
	 * @return 返回生成代码样式
	 */
	@GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // 加入流式响应的声明
	public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
	                                  @RequestParam String message,
	                                  HttpServletRequest request) {
		// 1.参数校验
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID错误");
		ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
		// 2.获取当前登录用户
		User loginUser = userService.getLoginUser(request);
		// 3.直接调用 门面模式下的AI生成代码服务 生成代码 （SSE流式返回）
		Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
		// 4.对输出流处理 - 转换为ServerSentEvent格式
		return contentFlux
				// 封装第一层
				.map(chunk -> {
					// 在每一个输出的流前添加一个字符"d"，如输出{"d":代码}
					Map<String, String> wrapper = Map.of("d", chunk);
					String jsonData = JSONUtil.toJsonStr(wrapper);
					// 封装第二层 (SSE事件流，以String字符串形式返回)
					return ServerSentEvent.<String>builder()
							// 把第一层得到的 jsonData 封装到返回的SSE事件流中
							.data(jsonData)
							.build();
				})
				.concatWith(Mono.just(
						// 发送结束事件
						ServerSentEvent.<String>builder()
							.event("done")
							.data("")
							.build()
				));
	}


	/**
	 * 应用部署
	 *
	 * @param appDeployRequest 部署请求
	 * @param request 登录请求
	 * @return 部署后的 URL
	 */
	@PostMapping("/deploy")
	public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
		// 检查部署请求是否为空
		ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
		// 获取应用Id
		Long appId = appDeployRequest.getAppId();
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用Id不能为空");
		// 获取当前登录用户
		User loginUser = userService.getLoginUser(request);
		// 调用服务部署应用
		String deployUrl = appService.deployApp(appId, loginUser);
		// 返回部署成功的 URL
		return ResultUtils.success(deployUrl);
	}


	/**
	 * 新增App应用
	 *
	 * @param appAddRequest 新增应用请求
	 * @param request       登录请求
	 * @return 应用id
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {

		// 参数校验
		ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
		String initPrompt = appAddRequest.getInitPrompt();
		ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
		// 获取当前登录用户
		User loginUser = userService.getLoginUser(request);
		// 构造入库对象 - App
		App app = new App();
		// 将请求中的数据赋值给新的App对象
		BeanUtils.copyProperties(appAddRequest, app);
		// 给App中的用户设置id，从登录用户获取对应的id
		app.setUserId(loginUser.getId());

		// 应用名称暂时为 initPrompt 前12位
		app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
		// 暂时设置为多文件生成
		app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());

		// 插入数据库
		boolean result = appService.save(app);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(app.getId());
	}


	/**
	 * 更新应用（用户只能更新自己的应用名称）
	 *
	 * @param appUpdateRequest 更新请求
	 * @param request          登录请求
	 * @return 更新结果
	 */
	@PostMapping("/update")
	public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
		// 1.校验参数
		if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 2.获取登录用户，并判断当前id对应的App是否存在
		User loginUser = userService.getLoginUser(request);
		Long id = appUpdateRequest.getId();
		App oldApp = appService.getById(id);
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		// 3.仅本人可以更新应用
		if (!oldApp.getUserId().equals(loginUser.getId())) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 否则，可以更新应用,设置app更新的参数 (id、appName和 编辑时间)
		App app = new App();
		app.setId(id);
		app.setAppName(appUpdateRequest.getAppName());
		app.setEditTime(LocalDateTime.now());
		// 4.更新数据库
		boolean result = appService.updateById(app);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
		return ResultUtils.success(true);
	}


	/**
	 * 删除应用（用户只能删除自己的应用）
	 *
	 * @param deleteRequest 删除请求
	 * @param request       登录请求
	 * @return 删除结果
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		// 1.校验数据
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 2.获取登录用户
		User loginUser = userService.getLoginUser(request);
		Long id = deleteRequest.getId();
		log.info("尝试删除应用 - ID: {}, 用户ID: {}, 用户角色: {}", id, loginUser.getId(), loginUser.getUserRole());
		
		// 3.判断当前请求中的id是否存在
		App oldApp = appService.getById(id);
		if (oldApp == null) {
			log.warn("删除失败 - 应用不存在，ID: {}", id);
		} else {
			log.info("找到应用 - ID: {}, 应用名: {}, 所属用户ID: {}", oldApp.getId(), oldApp.getAppName(), oldApp.getUserId());
		}
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		
		// 4.权限 - 仅本人或管理员可以删除
		if (!oldApp.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
			log.warn("删除失败 - 权限不足，应用所属用户: {}, 当前用户: {}", oldApp.getUserId(), loginUser.getId());
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		
		// 5.最后操作数据库
		boolean result = appService.removeById(id);
		log.info("删除应用结果 - ID: {}, 结果: {}", id, result);
		return ResultUtils.success(result);
	}


	/**
	 * 根据id获取应用详情
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/app/vo")
	public BaseResponse<AppVO> getAppVOById(long id) {
		// 1.参数校验
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 2.查询数据库
		App app = appService.getById(id);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
		// 3.数据存在则获取封装类（含用户信息）
		return ResultUtils.success(appService.getAppVO(app));
	}


	/**
	 * 分页获取当前用户创建的应用列表
	 * @param appQueryRequest 查询请求
	 * @param request 登录请求
	 * @return 用户查询到的自己的应用列表
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
		// 1.校验参数
		ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
			// 限制每页最多显示20个信息
		long pageSize = appQueryRequest.getPageSize();
		ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20个应用");
		long pageNum = appQueryRequest.getPageNum();
		// 2.只允许查询当前用户的应用
		appQueryRequest.setUserId(loginUser.getId());
		QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
		Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);  // 根据查询条件进行分页操作，且按照pageNum和pageSize
		// 3.数据封装 - VO
		Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
		List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());  // 根据appPage的值获取appVOList
			// 最后设置VO列表到分页对象
		appVOPage.setRecords(appVOList);
		return ResultUtils.success(appVOPage);
	}


	/**
	 * 分页获取精选App的应用列表
	 * @param appQueryRequest 查询请求
	 * @param request 登录请求
	 * @return 查询到的精选App列表
	 */
	@PostMapping("/good/list/page/vo")
	public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
		// 1.校验参数
		ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
			// 限制每页最多20条信息
		long pageSize = appQueryRequest.getPageSize();
		ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20个应用");
		long pageNum = appQueryRequest.getPageNum();

		// 2.只查询 精选的应用
		appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
		QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
			// 分页查询
		Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
		// 3.数据封装 - VO
		Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow()); // 新建一个VO的分页对象
		List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
		appVOPage.setRecords(appVOList);
		return ResultUtils.success(appVOPage);
	}


	/**
	 * 管理员删除应用
	 *
	 * @param deleteRequest 删除请求
	 * @return 删除结果
	 */
	@PostMapping("/admin/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
		// 1.校验参数
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Long id = deleteRequest.getId();
		// 2.判断id对应的app在数据库中是否存在
		App oldApp = appService.getById(id);
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		// 3.否则，进行删除
		boolean result = appService.removeById(id);
		return ResultUtils.success(result);
	}


	/**
	 * 管理员更新应用
	 * @param appAdminUpdateRequest 管理员更新应用请求
	 * @return 更新结果
	 */
	@PostMapping("/admin/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
		// 1.参数校验
		if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 2.拿到id后判断对应的app是否存在
		Long id = appAdminUpdateRequest.getId();
		App oldApp = appService.getById(id);
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		// 3.更新APP操作：先创建新的App对象，复制属性后，设置编辑时间 -> 更新APP
		App app = new App();
		BeanUtils.copyProperties(appAdminUpdateRequest, app);
		app.setEditTime(LocalDateTime.now());
		boolean result = appService.updateById(app);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}


	/**
	 * 管理员分页获取应用列表
	 *
	 * @param appQueryRequest 应用查询请求
	 * @param request 登录请求
	 * @return 管理员查到的分页列表
	 */
	@PostMapping("/admin/list/page/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<AppVO>> listAppVOByPageAdmin(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
		// 1.校验参数
		ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
		long pageNum = appQueryRequest.getPageNum();
		long pageSize = appQueryRequest.getPageSize();
		QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
			// 分页查询
		Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);

		// 2.数据封装
		Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
		List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
			// 把得到的值赋给appVOPage
		appVOPage.setRecords(appVOList);
		return ResultUtils.success(appVOPage);
	}


	/**
	 * 管理员根据id获取应用详情
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/admin/get/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<AppVO> getAppVOByIdByAdmin(long id) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		App app = appService.getById(id);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(appService.getAppVO(app));
	}
}
