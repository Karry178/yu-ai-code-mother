package com.yupi.yucodemotherbackend.model.vo;

import com.mybatisflex.annotation.Column;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 脱敏后的登录用户信息(VO)
 */
@Data
public class LoginUserVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;

	/**
	 * 账号
	 */
	@Column("userAccount")
	private String userAccount;

	/**
	 * 用户昵称
	 */
	@Column("userName")
	private String userName;

	/**
	 * 用户头像
	 */
	@Column("userAvatar")
	private String userAvatar;

	/**
	 * 用户简介
	 */
	@Column("userProfile")
	private String userProfile;

	/**
	 * 用户角色：user/admin
	 */
	@Column("userRole")
	private String userRole;

	/**
	 * 编辑时间
	 */
	@Column("editTime")
	private LocalDateTime editTime;

	/**
	 * 创建时间
	 */
	@Column("createTime")
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@Column("updateTime")
	private LocalDateTime updateTime;

}
