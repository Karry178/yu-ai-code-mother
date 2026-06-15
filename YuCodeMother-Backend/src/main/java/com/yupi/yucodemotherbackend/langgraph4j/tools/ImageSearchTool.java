package com.yupi.yucodemotherbackend.langgraph4j.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yucodemotherbackend.langgraph4j.enums.ImageCategoryEnum;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageResource;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片搜索工具（根据关键词搜索内容图片）
 */
@Slf4j
@Component
public class ImageSearchTool {

	private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";

	@Value("${pexels.api-key}")
	private String pexelsApiKey;

	/**
	 * 根据搜索关键词 搜 图片
	 * @param query
	 * @return
	 */
	@Tool("搜索内容相关的图片，用于网站内容展示")
	public List<ImageResource> searchContentImages(@P("搜索关键词") String query) {
		List<ImageResource> imageList = new ArrayList<>();
		int searchCount = 12;

		// 调用API，使用 try-with-resources 自动释放 HTTP 资源 -> 根据官方文档构建请求
		try (HttpResponse response = HttpRequest.get(PEXELS_API_URL)
				// 传递各种参数
				.header("Authorization", pexelsApiKey)
				.form("query", query)
				.form("per_page", searchCount)
				.form("page", 1)
				.execute()) {
			if (response.isOk()) {
				// 响应存在，则转为JSON格式输出结果
				JSONObject result = JSONUtil.parseObj(response.body());
				// 取出响应值结果中的 photos 响应列表
				JSONArray photos = result.getJSONArray("photos");
				for (int i = 0; i < photos.size(); i++) {
					JSONObject photo = photos.getJSONObject(i);
					JSONObject src = photo.getJSONObject("src");
					imageList.add(ImageResource.builder()
									.category(ImageCategoryEnum.CONTENT)
									.description(photo.getStr("alt", query))
									.url(src.getStr("medium"))
							.build());
				}
			}
		} catch (Exception e) {
			log.error("Pexels API 调用失败：{}", e.getMessage(), e);
		}
		return imageList;
	}
}
