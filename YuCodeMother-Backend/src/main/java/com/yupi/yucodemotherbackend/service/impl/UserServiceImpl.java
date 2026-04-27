package com.yupi.yucodemotherbackend.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import static com.yupi.yucodemotherbackend.constatnt.UserConstant.USER_LOGIN_STATE;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.mapper.UserMapper;
import com.yupi.yucodemotherbackend.model.dto.user.UserQueryRequest;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.enums.UserRoleEnum;
import com.yupi.yucodemotherbackend.model.vo.LoginUserVO;
import com.yupi.yucodemotherbackend.model.vo.UserVO;
import com.yupi.yucodemotherbackend.service.UserService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 服务层实现。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

	/**
	 * 用户注册
	 *
	 * @param userAccount   用户账号
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @return
	 */
	@Override
	public long UserRegister(String userAccount, String userPassword, String checkPassword) {
		// 1.校验参数
		if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "账号长度过短");
		ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度过短");
		ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
		// 2.查询用户是否存在 - 使用QueryWrapper
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.eq("userAccount", userAccount);
		long count = this.mapper.selectCountByQuery(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复了");
		}
		// 3.加密密码 - 调用getEncryptPassword
		String encryptPassword = getEncryptPassword(userPassword);
		// 4.创建用户，插入数据库
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptPassword);
		user.setUserName("无名氏");
		user.setUserRole(UserRoleEnum.USER.getValue());
		boolean saveResult = this.save(user);
		if (!saveResult) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败，数据库错误");
		}
		return 0;
	}


	/**
	 * 获取脱敏后的用户信息，用LoginUserVO包装
	 *
	 * @param user
	 * @return
	 */
	@Override
	public LoginUserVO getLoginUserVO(User user) {
		// 1.校验
		if (user == null) {
			return null;
		}
		// 2.转换为脱敏的数据
		LoginUserVO loginUserVO = new LoginUserVO();
		BeanUtil.copyProperties(user, loginUserVO);
		return loginUserVO;
	}


	/**
	 * 用户登录
	 *
	 * @param userAccount  用户账号
	 * @param userPassword 用户密码
	 * @param request      登录请求
	 * @return
	 */
	@Override
	public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
		// 1.校验参数
		ThrowUtils.throwIf(userAccount == null && userPassword == null, ErrorCode.PARAMS_ERROR);
		if (request == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录请求为空");
		}
		// 2.加密 - 调用加密方法 getEncryptPassword()
		String encryptPassword = this.getEncryptPassword(userPassword);
		// 3.查询用户是否存在
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.eq("userAccount", userAccount);
		queryWrapper.eq("userPassword", encryptPassword);
		User user = this.mapper.selectOneByQuery(queryWrapper);
		if (user == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不存在或者密码错误");
		}
		// ⭐️ 4.如果用户存在，记录用户登录态 - 获取request中的Session，然后设置相应属性Attribute
		request.getSession().setAttribute(USER_LOGIN_STATE, user);
		// 5.返回脱敏后的用户信息
		return this.getLoginUserVO(user);
	}


	/**
	 * 获取当前登录用户
	 *
	 * @param request 登录请求
	 * @return
	 */
	@Override
	public User getLoginUser(HttpServletRequest request) {
		// 1.服务端判断当前用户是否登录
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		User currentUser = (User) userObj;
		// 2.校验
		if (currentUser == null || currentUser.getId() == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
		// 3.从数据库查询当前用户信息 (保证一直拿到的都是最新数据，缓存中的可能非最新数据)
		Long userId = currentUser.getId();
		currentUser = this.getById(userId);
		if (currentUser == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
		return currentUser;
	}


	/**
	 * 获取脱敏后的用户信息
	 *
	 * @param user 用户信息
	 * @return
	 */
	@Override
	public UserVO getUserVO(User user) {
		// 1.校验
		if (user == null) {
			return null;
		}
		// 2.从User拿到UserVO
		UserVO userVO = new UserVO();
		BeanUtil.copyProperties(user, userVO);
		return userVO;
	}


	/**
	 * 获取脱敏后的用户信息（分页）
	 *
	 * @param userList 用户列表
	 * @return
	 */
	@Override
	public List<UserVO> getUserVOList(List<User> userList) {
		// 1.校验
		if (CollUtil.isEmpty(userList)) {
			return new ArrayList<>();
		}
		// 2.用Stream流，将userList转为userVOList
		return userList.stream()
				// 将列表中每一个user通过getUserVO()方法转为UserVO
				.map(this::getUserVO)
				.collect(Collectors.toList());
	}


	/**
	 * 根据查询条件构造数据查询参数
	 *
	 * @param userQueryRequest 用户查询请求
	 * @return
	 */
	@Override
	public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
		// 1.校验参数
		if (userQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
		}
		// 2.拿到userQueryRequest请求对象中的值
		Long id = userQueryRequest.getId();
		String userName = userQueryRequest.getUserName();
		String userAccount = userQueryRequest.getUserAccount();
		String userProfile = userQueryRequest.getUserProfile();
		String userRole = userQueryRequest.getUserRole();
		String sortField = userQueryRequest.getSortField();
		String sortOrder = userQueryRequest.getSortOrder();

		// 3.创建QueryWrapper，只在字段不为空时添加查询条件
		QueryWrapper queryWrapper = QueryWrapper.create();
		
		// 精确匹配
		if (id != null) {
			queryWrapper.eq("id", id);
		}
		if (StrUtil.isNotBlank(userAccount)) {
			queryWrapper.eq("userAccount", userAccount);
		}
		if (StrUtil.isNotBlank(userRole)) {
			queryWrapper.eq("userRole", userRole);
		}
		
		// 模糊匹配
		if (StrUtil.isNotBlank(userName)) {
			queryWrapper.like("userName", userName);
		}
		if (StrUtil.isNotBlank(userProfile)) {
			queryWrapper.like("userProfile", userProfile);
		}
		
		// 排序
		if (StrUtil.isNotBlank(sortField)) {
			boolean isAsc = "ascend".equals(sortOrder);
			if (isAsc) {
				queryWrapper.orderBy(sortField, true);
			} else {
				queryWrapper.orderBy(sortField, false);
			}
		}
		
		return queryWrapper;
	}


	/**
	 * 用户注销
	 *
	 * @param request 浏览器请求
	 * @return
	 */
	@Override
	public boolean userLogout(HttpServletRequest request) {
		// 1.先判断用户是否登录
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		if (userObj == null) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
		}
		// 2.否则，移除登录态
		request.getSession().removeAttribute(USER_LOGIN_STATE);
		return true;
	}


	/**
	 * 盐值加密 Salt = Karry
	 *
	 * @param userPassword
	 * @return
	 */
	@Override
	public String getEncryptPassword(String userPassword) {
		// 盐值加密
		final String SALT = "Karry";
		return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes(StandardCharsets.UTF_8));
	}
}
