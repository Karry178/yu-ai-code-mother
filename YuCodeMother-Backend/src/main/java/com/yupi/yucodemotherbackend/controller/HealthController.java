package com.yupi.yucodemotherbackend.controller;

import com.yupi.yucodemotherbackend.common.BaseResponse;
import com.yupi.yucodemotherbackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口
 */
@RestController
@RequestMapping("/health")
public class HealthController {

	@GetMapping("/")
	public BaseResponse<String> healthCheck() {
		return ResultUtils.success("ok");
	}
}
