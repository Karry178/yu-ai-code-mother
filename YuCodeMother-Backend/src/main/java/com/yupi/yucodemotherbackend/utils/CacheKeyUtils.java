package com.yupi.yucodemotherbackend.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存 key 生成工具类
 */
public class CacheKeyUtils {

	/**
	 * 根据对象生成缓存Key（JSON + MD5）
	 *
	 * @param object 要生成key的对象
	 * @return MD5哈希后的缓存key
	 */
	public static String generateKey(Object object) {
		// 空对象也要有缓存的key，避免缓存穿透
		if (object == null) {
			return DigestUtil.md5Hex("null");
		}
		// 将对象先转为JSON -> 再转为 MD5：先转json目的是每个缓存对象都用键值对保存，再转md5是因为json占用大，md5加密占内存更少
		String jsonStr = JSONUtil.toJsonStr(object);
		return DigestUtil.md5Hex(jsonStr);
	}
}
