package com.yupi.yucodemotherbackend.service.impl;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.yupi.yucodemotherbackend.core.handler.JsonMessageStreamHandler;
import com.yupi.yucodemotherbackend.core.handler.StreamHandlerExecutor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yupi.yucodemotherbackend.constatnt.AppConstant;
import com.yupi.yucodemotherbackend.core.AiCodeGeneratorFacade;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.mapper.AppMapper;
import com.yupi.yucodemotherbackend.model.dto.app.AppQueryRequest;
import com.yupi.yucodemotherbackend.model.entity.App;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.enums.ChatHistoryMessageTypeEnum;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import com.yupi.yucodemotherbackend.model.vo.AppVO;
import com.yupi.yucodemotherbackend.model.vo.UserVO;
import com.yupi.yucodemotherbackend.service.AppService;
import com.yupi.yucodemotherbackend.service.ChatHistoryService;
import com.yupi.yucodemotherbackend.service.UserService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

	// 引入userService
	@Resource
	private UserService userService;

	// 引入门面 - 调用AI
	@Resource
	private AiCodeGeneratorFacade aiCodeGeneratorFacade;

	// 引入ChatHistoryService
	@Resource
	private ChatHistoryService chatHistoryService;

	// 引入流处理器执行器
	@Resource
	private StreamHandlerExecutor streamHandlerExecutor;


	/**
	 * 【重点】通过对话生成应用代码
	 *
	 * @param appId 应用Id
	 * @param message 提示词
	 * @param loginUser 登录用户
	 * @return
	 */
	@Override
	public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
		// 1.参数校验
		if (appId == null || appId <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用Id错误");
		}
		ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
		// 2.查询应用信息
		App app = this.getById(appId);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
		// 3.权限校验，仅本人可以和自己的应用对话
		if (!app.getUserId().equals(loginUser.getId())) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
		}
		// 4.获取应用的代码生成类型
		String codeGenType = app.getCodeGenType();
			// 获取app对应的枚举类的值
		CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
		ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");

		// 5.在调用AI前，将用户消息保存在数据库中
		chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
		// 6.调用AI生成代码 (流式)
		Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
		// 7.【重点】收集 AI 响应的内容，并在完成后保存记录到对话历史
		/*StringBuilder aiResponseBuilder = new StringBuilder();
			// 反应式编程
		return contentFlux.map(chunk -> {
			// 实时收集 AI 响应的内容
			aiResponseBuilder.append(chunk);
			return chunk;
		}).doOnComplete(() -> {
			// 流式返回完成后，保存 AI 消息到对话历史中
			String aiResponse = aiResponseBuilder.toString();  // 拿到 message 返回
			chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
		}).doOnError(error -> {
			// 即使 AI 回复失败，也需要保存错误记录
			String errorMessage = "AI 回复信息失败：" + error.getMessage();
			chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
		});*/

		/**
		 * 因为有了处理器：JsonMessageStreamHandler、SimpleTextStreamHandler和StreamHandlerExecutor
		 * -> 上述收集AI相应内容并处理原始流后返回给前端的功能,就不用写在外层(App的实现类)了
		 * -> 直接调用流处理器执行器 StreamHandlerExecutor
 		 */
		return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);

	}


	/**
	 * 【重点】应用部署
	 *
	 * @param appId 应用Id
	 * @param loginUser 登录用户，目的是进行权限校验
	 * @return 可访问的部署地址
	 */
	@Override
	public String deployApp(Long appId, User loginUser) {
		// 1.参数校验
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID错误");
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
		// 2.查询应用信息
		App app = this.getById(appId);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
		// 3.权限校验，仅本人可以部署自己的应用
		if (!app.getUserId().equals(loginUser.getId())) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
		}
		// 4.检查是否已有 deployKey
		String deployKey = app.getDeployKey();
			// 如果没有，则生成 6 位 deployKey (字母 + 数字)
		if (StrUtil.isBlank(deployKey)) {
			deployKey = RandomUtil.randomString(6);
		}

		// 5.获取代码生成类型，获取原始代码生成路径（应用访问目录）
		String codeGenType = app.getCodeGenType();
		String sourceDirName = codeGenType + "_" + appId;
			// 生成原始文件路径
		String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

		// 6.检查路径是否存在
			// 先新建一个File对象，放入新建的sourceDirPath
		File sourceDir = new File(sourceDirPath);
			// 如果文件路径不存在 或者 路径不是一个目录形式，报错
		if (!sourceDir.exists() || !sourceDir.isDirectory()) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码路径不存在，请先生成应用");
		}

		// 7.复制文件到部署目录
		String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
			// FileUtil.copyContent(原始文件目录， 目标文件目录， 是否覆盖内容)
		try {
			FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败：" + e.getMessage());
		}

		// 8.更新数据库
		App updateApp = new App();
		updateApp.setId(appId);
		updateApp.setDeployKey(deployKey);
		updateApp.setDeployedTime(LocalDateTime.now());
		boolean updateResult = this.updateById(updateApp);
		ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
		// 9.返回可访问的 URL 地址
		return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
	}


	/**
	 * 获取应用封装类
	 *
	 * @param app
	 * @return
	 */
	@Override
	public AppVO getAppVO(App app) {
		// 1.校验参数
		if (app == null) {
			return null;
		}
		// 2.定义一个AppVO对象，从app赋值
		AppVO appVO = new AppVO();
		BeanUtils.copyProperties(app, appVO);
		// 3.关联查询用户对象
		Long userId = app.getUserId();
		// 如果userId非空，拿到对应的user后操作数据库，返回userVO
		if (userId != null) {
			User user = userService.getById(userId);
			UserVO userVO = userService.getUserVO(user);
			// 最后，把userVO的信息给appVO返回
			appVO.setUser(userVO);
		}
		return appVO;
	}


	/**
	 * 获取应用封装类列表
	 *
	 * @param appList 应用列表
	 * @return 应用封装列表
	 */
	@Override
	public List<AppVO> getAppVOList(List<App> appList) {
		// 1.校验参数
		if (CollUtil.isEmpty(appList)) {
			return new ArrayList<>();
		}
		// 2.批量获取用户信息，避免 N+1 查询问题
			// 取出所有的用户Id为一个集合
		Set<Long> userIds = appList.stream()
				.map(App::getUserId)
				.collect(Collectors.toSet());
			// 对用户id集合按照id列表查询用户信息，转为map
		Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
				.collect(Collectors.toMap(User::getId, userService::getUserVO));
			// 遍历app列表，给每个app设置一个值，最后返回appVO封装类
		return appList.stream().map(app -> {
			AppVO appVO = getAppVO(app);
			UserVO userVO = userVOMap.get(app.getUserId()); // 从map中根据用户id取出封装后的信息
			appVO.setUser(userVO);
			return appVO;
		}).collect(Collectors.toList());
	}


	/**
	 * 构造应用查询条件
	 *
	 * @param appQueryRequest 应用查询请求
	 * @return
	 */
	@Override
	public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
		// 1.校验参数
		if (appQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
		}
		// 2.获取全部的参数 allget
		Long id = appQueryRequest.getId();
		String appName = appQueryRequest.getAppName();
		String cover = appQueryRequest.getCover();
		String initPrompt = appQueryRequest.getInitPrompt();
		String codeGenType = appQueryRequest.getCodeGenType();
		String deployKey = appQueryRequest.getDeployKey();
		Integer priority = appQueryRequest.getPriority();
		Long userId = appQueryRequest.getUserId();
		String sortField = appQueryRequest.getSortField();
		String sortOrder = appQueryRequest.getSortOrder();
		// 3.返回QueryWrapper查询的结果
		return QueryWrapper.create()
				.eq("id", id)
				.like("appName", appName)
				.like("cover", cover)
				.like("initPrompt", initPrompt)
				.eq("codeGenType", codeGenType)
				.eq("deployKey", deployKey)
				.eq("priority", priority)
				.eq("userId" ,userId)
				.orderBy(sortField, "ascend".equals(sortOrder));
	}


	/**
	 * 删除应用时，关联删除对话历史
	 * 重新覆盖原有默认方法，要用Serializable(序列化格式)修饰 appId (默认如此)
	 *
	 * @param id 应用Id
	 * @return
	 */
	@Override
	public boolean removeById(Serializable id) {

		if (id == null) {
			return false;
		}
		// 将id的序列化格式转为Long类型
		long appId = Long.parseLong(id.toString());
		if (appId <= 0) {
			return false;
		}
		// 调用删除对话历史方法 -> 先删除关联的对话历史
		try {
			chatHistoryService.deleteByAppId(appId);
		} catch (Exception e) {
			log.error("删除应用关联的对话历史失败：{}", e.getMessage());
		}
		// 删除应用 - 调用自己的删除方法
		// 用super不用this是因为：用this会调用当前类重写的removeById()方法，导致调用自己 -> 无限递归 -> StackOverflowError; super.removeById(id)调用的是父类ServiceImpl的removeById方法
		return super.removeById(id);
	}
}
