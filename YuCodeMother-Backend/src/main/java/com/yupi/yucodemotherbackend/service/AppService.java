package com.yupi.yucodemotherbackend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yupi.yucodemotherbackend.model.dto.app.AppQueryRequest;
import com.yupi.yucodemotherbackend.model.entity.App;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
public interface AppService extends IService<App> {

	/**
	 * 通过对话生成应用代码
	 *
	 * @param appId 应用Id
	 * @param message 提示词
	 * @param loginUser 登录用户
	 * @return
	 */
	Flux<String> chatToGenCode(Long appId, String message, User loginUser);


	/**
	 * 应用部署
	 *
	 * @param appId 应用Id
	 * @param loginUser 登录用户，目的是进行权限校验
	 * @return 可访问的部署地址
	 */
	String deployApp(Long appId, User loginUser);


	/**
	 * 获取应用封装类
	 *
	 * @param app
	 * @return
	 */
	AppVO getAppVO(App app);


	/**
	 * 获取应用封装类列表
	 *
	 * @param appList 应用列表
	 * @return 应用封装列表
	 */
	List<AppVO> getAppVOList(List<App> appList);


	/**
	 * 构造应用查询条件
	 *
	 * @param appQueryRequest 应用查询请求
	 * @return
	 */
	QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


}
