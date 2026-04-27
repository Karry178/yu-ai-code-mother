package com.yupi.yucodemotherbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.yupi.yucodemotherbackend.annotation.AuthCheck;
import com.yupi.yucodemotherbackend.common.BaseResponse;
import com.yupi.yucodemotherbackend.common.DeleteRequest;
import com.yupi.yucodemotherbackend.common.PageRequest;
import com.yupi.yucodemotherbackend.common.ResultUtils;
import com.yupi.yucodemotherbackend.constatnt.UserConstant;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.model.dto.user.*;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.vo.LoginUserVO;
import com.yupi.yucodemotherbackend.model.vo.UserVO;
import com.yupi.yucodemotherbackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 控制层。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;


	/**
	 * 用户注册
	 *
	 * @param userRegisterRequest 用户注册请求
	 * @return
	 */
	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
		// 1.校验
		ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
		// 2.获取userRegisterRequest的值,并将结果返回给result
		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		// 调用Service层将数据写入数据库中
		long result = userService.UserRegister(userAccount, userPassword, checkPassword);
		return ResultUtils.success(result);
	}


	/**
	 * 用户登录
	 *
	 * @param userLoginRequest 用户登录请求
	 * @param request          登录请求
	 * @return
	 */
	@PostMapping("/login")
	public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
		// 1.校验
		ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
		// 2.获取用户账密后脱敏返回
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
		return ResultUtils.success(loginUserVO);
	}


	/**
	 * 获取当前登录用户
	 *
	 * @param request 登录请求
	 * @return
	 */
	@PostMapping("/get/login")
	public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
		// 直接通过Service拿数据库数据
		User loginUser = userService.getLoginUser(request);
		// 返回给前端之前 需要 通过VO方法过滤，通过userService
		return ResultUtils.success(userService.getLoginUserVO(loginUser));
	}


	/**
	 * 用户注销
	 *
	 * @param request 浏览器请求
	 * @return
	 */
	@PostMapping("/logout")
	public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
		ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
		// 调用userService，操作数据库
		boolean result = userService.userLogout(request);
		return ResultUtils.success(result);
	}


	/**
	 * 创建用户，成功返回用户Id
	 *
	 * @param userAddRequest 添加用户请求
	 * @return
	 */
	@PostMapping("/add")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
		ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
		User user = new User();
		// 从添加用户请求中复制
		BeanUtil.copyProperties(userAddRequest, user);
		// 设置默认密码,并加密
		final String DEFAULT_PASSWORD = "12345678";
		String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
		user.setUserPassword(encryptPassword);
		// 把用户保存在数据库中
		boolean result = userService.save(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(user.getId());
	}


	/**
	 * 根据id获取包装类
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/get/vo")
	public BaseResponse<UserVO> getUserVOById(long id) {
		User user = userService.getById(id);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
		return ResultUtils.success(userService.getUserVO(user));
	}


	/**
	 * 管理员 删除用户
	 *
	 * @param deleteRequest 删除请求
	 * @return
	 */
	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
		ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
		// 操作数据库，根据id删除数据
		boolean result = userService.removeById(deleteRequest.getId());
		return ResultUtils.success(result);
	}


	/**
	 * 更新用户信息
	 *
	 * @param userUpdateRequest 用户信息更新请求
	 * @return
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
		// 1.校验
		ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
		// 2.定义User，从userUpdateRequest中复制新数据后更新数据库
		User user = new User();
		BeanUtils.copyProperties(userUpdateRequest, user);
		boolean result = userService.updateById(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}


	/**
	 * ⭐️ 分页获取用户封装列表(仅管理员)
	 *
	 * @param userQueryRequest 用户分页请求
	 * @return
	 */
	@PostMapping("/list/page/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<UserVO>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest) {
		ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
		long pageNum = userQueryRequest.getPageNum();
		int pageSize = userQueryRequest.getPageSize();
		// 1.从数据库拿数据 - 第一个参数是包含了当前页码+页面页数的Page，第二个参数是按照查询条件去数据库拿数据
		Page<User> userPage = userService.page(Page.of(pageNum, pageSize), userService.getQueryWrapper(userQueryRequest));

		// 2.数据脱敏
		Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
		List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
		userVOPage.setRecords(userVOList);
		return ResultUtils.success(userVOPage);
	}


	/**
	 * 保存。
	 *
	 * @param user
	 * @return {@code true} 保存成功，{@code false} 保存失败
	 */
	@PostMapping("save")
	public boolean save(@RequestBody User user) {
		return userService.save(user);
	}

	/**
	 * 根据主键删除。
	 *
	 * @param id 主键
	 * @return {@code true} 删除成功，{@code false} 删除失败
	 */
	@DeleteMapping("remove/{id}")
	public boolean remove(@PathVariable Long id) {
		return userService.removeById(id);
	}

	/**
	 * 根据主键更新。
	 *
	 * @param user
	 * @return {@code true} 更新成功，{@code false} 更新失败
	 */
	@PutMapping("update")
	public boolean update(@RequestBody User user) {
		return userService.updateById(user);
	}

	/**
	 * 查询所有。
	 *
	 * @return 所有数据
	 */
	@GetMapping("list")
	public List<User> list() {
		return userService.list();
	}

	/**
	 * 根据主键获取。
	 *
	 * @param id 主键
	 * @return 详情
	 */
	@GetMapping("getInfo/{id}")
	public User getInfo(@PathVariable Long id) {
		return userService.getById(id);
	}

	/**
	 * 分页查询。
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("page")
	public Page<User> page(Page<User> page) {
		return userService.page(page);
	}

}
