package com.yupi.yucodemotherbackend.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yucodemotherbackend.langgraph4j.model.enums.ImageCategoryEnum;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageResource;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UndrawIllustrationTool {

	// 插画网站unDraw的接口API
	private static final String UNDRAW_API_URL = "https://undraw.co/_next/data/nS41BRGVYK4TTVjGNap_q/search/%s.json?term=%s";

	@Tool("搜索插画图片，用于网站美化和装饰")
	public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
		List<ImageResource> imageList = new ArrayList<>();
		int searchCount = 12;
		String apiUrl = String.format(UNDRAW_API_URL, query, query);

		// 使用 try-with-resources 自动释放 HTTP 资源
		try (HttpResponse response = HttpRequest.get(apiUrl).timeout(10000).execute()) {

			if (!response.isOk()) {
				return imageList;
			}
			// 解析响应体为 JSON 对象，并提取 pageProps 字段（包含页面数据）
			JSONObject result = JSONUtil.parseObj(response.body());
				// pageProps：是 Next.js 中 getServerSideProps 或 getStaticProps 返回给页面组件的数据，
				// -> 会被序列化到这个 JSON 响应中。对 unDraw 来说，搜索结果的插画列表就存放在 pageProps.initialResults 里。
			JSONObject pageProps = result.getJSONObject("pageProps");
			if (pageProps == null) {
				return imageList;
			}
			// 从 pageProps 中获取初始搜索结果数组（即插画列表数据）
			JSONArray initialResults = pageProps.getJSONArray("initialResults");
			if (initialResults == null || initialResults.isEmpty()) {
				return imageList;
			}
			// 遍历搜索结果，将每个插画转换为 ImageResource 对象
			int actualCount = Math.min(searchCount, initialResults.size());

			for (int i = 0; i < actualCount; i++) {
				JSONObject illustration = initialResults.getJSONObject(i);
				String title = illustration.getStr("title", "插画");
				String media = illustration.getStr("media", "");
				if (StrUtil.isNotBlank(media)) {
					imageList.add(ImageResource.builder()
									.category(ImageCategoryEnum.ILLUSTRATION)
									.description(title)
									.url(media)
									.build());
				}
			}
		} catch (Exception e) {
			log.error("搜索插画失败：{}", e.getMessage(), e);
		}
		return imageList;
	}
}
