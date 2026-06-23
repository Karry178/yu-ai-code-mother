package com.yupi.yucodemotherbackend.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.yupi.yucodemotherbackend.langgraph4j.model.enums.ImageCategoryEnum;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageResource;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LOGO 图片生成工具
 */
@Slf4j
@Component
public class LogoGeneratorTool {

	// 引入 阿里百炼大模型的API_KEY
	@Value("${dashscope.api-key}")
	private String dashscopeApiKey;

	// 引入 阿里云生成图片大模型
	@Value("${dashscope.image-model:wan2.2-t2i-flash}")
	private String imageModel;

	@Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
	public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
		List<ImageResource> logoList = new ArrayList<>();
		try {
			// 构建Logo 设计提示词 + 构建 Logo 图片参数
			String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);
			ImageSynthesisParam param = ImageSynthesisParam.builder()
					.apiKey(dashscopeApiKey)
					.model(imageModel)
					.prompt(logoPrompt)
					.size("512*512")
					.n(1)
					.build();

			// 新建图片的ImageSynthesis后，根据构建的图片参数生成图片
			ImageSynthesis imageSynthesis = new ImageSynthesis();
			ImageSynthesisResult result = imageSynthesis.call(param);
			if (result != null && result.getOutput() != null && result.getOutput().getResults() != null) {
				// 根据生成的图片拿到图片列表
				List<Map<String, String>> results = result.getOutput().getResults();
				for (Map<String, String> imageResult : results) {
					String imageUrl = imageResult.get("url");
					if (StrUtil.isNotBlank(imageUrl)) {
						// 构建最后的logo图片对象
						logoList.add(ImageResource.builder()
										.category(ImageCategoryEnum.LOGO)
										.description(description)
										.url(imageUrl)
										.build());
					}
				}
			}
		} catch (Exception e) {
			log.error("生成 Logo 失败：{}", e.getMessage(), e);
		}
		return logoList;
	}
}
