package com.yupi.yucodemotherbackend.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类。
 *
 * @author <a href="https://github.com/Karry178">程序员Karry178</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id,不要用自增的id(Auto)，很容易被爬虫爬到，直接使用雪花ID(Generator), value = KeyGenerators.snowFlakeId
	 */
	@Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
	private Long id;

	/**
	 * 账号
	 */
	@Column("userAccount")
	private String userAccount;

	/**
	 * 密码
	 */
	@Column("userPassword")
	private String userPassword;

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

	/**
	 * 是否删除
	 */
	@Column(value = "isDelete", isLogicDelete = true)
	private Integer isDelete;

}
