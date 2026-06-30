package com.yupi.yucodemotherbackend.monitor;

import lombok.extern.slf4j.Slf4j;

/**
 * 监控上下文持有者（同线程内共享）
 */
@Slf4j
public class MonitorContextHolder {

	/**
	 * ThreadLocal 本质是一个HashMap，每一个线程Id对应一个value
	 */
	private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

	/**
	 * 设置监控上下文
	 *
	 * @param context
	 */
	public static void setContext(MonitorContext context) {
		CONTEXT_HOLDER.set(context);
	}

	/**
	 * 获取当前监控上下文
	 *
	 * @return
	 */
	public static MonitorContext getContext() {
		return CONTEXT_HOLDER.get();
	}

	/**
	 * 清楚监控上下文
	 */
	public static void clearContext() {
		CONTEXT_HOLDER.remove();
	}
}
