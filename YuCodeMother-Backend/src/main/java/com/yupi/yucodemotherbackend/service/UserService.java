package com.yupi.yucodemotherbackend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yupi.yucodemotherbackend.model.dto.user.UserQueryRequest;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.vo.LoginUserVO;
import com.yupi.yucodemotherbackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 服务层。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
public interface UserService extends IService<User> {

	/**
	 * 用户注册
	 *
	 * @param userAccount   用户账号
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @return
	 */
	long UserRegister(String userAccount, String userPassword, String checkPassword);


	/**
	 * 获取脱敏后的用户信息，用LoginUserVO包装
	 *
	 * @param user
	 * @return
	 */
	LoginUserVO getLoginUserVO(User user);


	/**
	 * 用户登录
	 *
	 * @param userAccount  用户账号
	 * @param userPassword 用户密码
	 * @param request      登录请求
	 * @return
	 */
	LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


	/**
	 * 获取当前登录用户
	 *
	 * @param request 浏览器请求
	 * @return
	 */
	User getLoginUser(HttpServletRequest request);


	/**
	 * 获取脱敏后的用户信息
	 *
	 * @param user 用户信息
	 * @return
	 */
	UserVO getUserVO(User user);


	/**
	 * 获取脱敏后的用户信息（分页）
	 *
	 * @param userList 用户列表
	 * @return
	 */
	List<UserVO> getUserVOList(List<User> userList);


	/**
	 * 根据查询条件构造数据查询参数
	 *
	 * @param userQueryRequest 用户查询请求
	 * @return
	 */
	QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);


	/**
	 * 用户注销
	 *
	 * @param request 浏览器请求
	 * @return
	 */
	boolean userLogout(HttpServletRequest request);


	/**
	 * 盐值加密 Salt = Karry
	 *
	 * @param userPassword
	 * @return
	 */
	String getEncryptPassword(String userPassword);

}
