package com.yupi.yucodemotherbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yupi.yucodemotherbackend.constatnt.AppConstant;
import com.yupi.yucodemotherbackend.core.AiCodeGeneratorFacade;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.model.dto.app.AppQueryRequest;
import com.yupi.yucodemotherbackend.model.entity.App;
import com.yupi.yucodemotherbackend.mapper.AppMapper;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import com.yupi.yucodemotherbackend.model.vo.AppVO;
import com.yupi.yucodemotherbackend.model.vo.UserVO;
import com.yupi.yucodemotherbackend.service.AppService;
import com.yupi.yucodemotherbackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 服务层实现。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

	// 引入userService
	@Resource
	private UserService userService;

	// 引入门面 - 调用AI
	@Resource
	private AiCodeGeneratorFacade aiCodeGeneratorFacade;


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
		// 5.调用AI生成代码
		return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
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
}
