package com.yupi.yucodemotherbackend.common;
import lombok.Data;

/**
 * 通用的 分页请求包装类，后续分页请求直接继承该请求
 */
@Data
public class PageRequest {

	/**
	 * 当前页码
	 */
	private int pageNum = 1;

	/**
	 * 页面大小 (最大页码)
	 */
	private int pageSize = 10;

	/**
	 * 排序字段
	 */
	private String sortField;

	/**
	 * 排序顺序（默认排序）
	 */
	private String sortOrder = "descend";
}
