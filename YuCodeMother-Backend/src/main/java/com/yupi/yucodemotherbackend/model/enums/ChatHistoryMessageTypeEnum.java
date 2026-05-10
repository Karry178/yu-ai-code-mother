package com.yupi.yucodemotherbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum ChatHistoryMessageTypeEnum {

	/**
	 * 对话历史消息枚举类型
	 */
	USER("用户", "user"),
	AI("AI", "ai");

	private final String text;

	private final String value;

	// 构造函数
	ChatHistoryMessageTypeEnum(String text, String value) {
		this.text = text;
		this.value = value;
	}


	/**
	 * 根据 value 获取枚举
	 *
	 * @param value 枚举值的value
	 * @return 枚举值
	 */
	public static ChatHistoryMessageTypeEnum getEnumByValue(String value) {
		if (ObjUtil.isEmpty(value)) {
			return null;
		}
		for (ChatHistoryMessageTypeEnum anEnum : ChatHistoryMessageTypeEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}
}
