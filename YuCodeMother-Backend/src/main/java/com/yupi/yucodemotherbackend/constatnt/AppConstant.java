package com.yupi.yucodemotherbackend.constatnt;

/**
 * 应用常量
 */
public interface AppConstant {

	/**
	 * 精选应用的优先级
	 */
	Integer GOOD_APP_PRIORITY = 99;

	/**
	 * 默认应用的优先级
	 */
	Integer DEFAULT_APP_PRIORITY = 0;

	/**
	 * 应用生成目录 -> 原来是"/YuCodeMother-Backend/tmp/code_output，但是测试时不会走现有tmp文件路径，而是会自动新建/YuCodeMother-Backend/tmp/code_output"
	 * -> 因为"user.dir"一般是模块根
	 */
	String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

	/**
	 * 应用部署目录 -> 同上修改
	 */
	String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

	/**
	 * 应用部署域名
	 */
	String CODE_DEPLOY_HOST = "http://localhost";
}
