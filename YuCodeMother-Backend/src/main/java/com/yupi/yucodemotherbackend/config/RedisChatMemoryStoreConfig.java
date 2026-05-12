package com.yupi.yucodemotherbackend.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


/**
 * Redis 持久化对话记忆 Config配置类
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

	private String host;

	private int port;

	/**
	 * spring.data.redis.username 对应 Redis ACL 用户名
	 */
	private String username;

	/**
	 * 兼容旧字段名
	 */
	private String user;

	private String password;

	private long ttl;

	@Bean
	public RedisChatMemoryStore redisChatMemoryStore() {
		// 先解析最终要使用的 Redis 用户名：
		// 1. 优先用 spring.data.redis.username
		// 2. 兼容旧字段 user
		// 3. 如果只配了密码但没配用户名，则默认用 Redis ACL 的 default 用户
		String redisUser = resolveRedisUser();

		// 构建 LangChain4j 使用的 Redis 聊天记忆存储
		return RedisChatMemoryStore.builder()
				.host(host)           // Redis 主机地址
				.port(port)           // Redis 端口
				.user(redisUser)      // Redis 用户名，ACL 模式下需要
				.password(password)   // Redis 密码
				.ttl(ttl)             // 聊天记录过期时间
				.build();
	}

	
	/**
	 * 解析 Redis 登录用户名。
	 * <p>
	 * LangChain4j 当前这版 RedisChatMemoryStore 在只传 password、不传 user 时，
	 * 不会按“带密码认证”方式创建连接，因此这里需要补齐用户名。
	 * </p>
	 */
	private String resolveRedisUser() {
		if (StringUtils.hasText(username)) {
			// 优先使用标准配置项 spring.data.redis.username
			return username;
		}
		if (StringUtils.hasText(user)) {
			// 兼容旧字段名 user
			return user;
		}
		if (StringUtils.hasText(password)) {
			// 如果配置了密码但没配用户名，默认使用 Redis 的 default 用户
			return "default";
		}
		// 没有用户名也没有密码，按无认证方式连接
		return null;
	}
}
