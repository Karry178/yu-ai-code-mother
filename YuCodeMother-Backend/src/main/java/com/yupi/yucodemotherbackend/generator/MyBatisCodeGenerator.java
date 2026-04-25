package com.yupi.yucodemotherbackend.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;


/**
 * MyBatis Flex 代码生成器
 */
public class MyBatisCodeGenerator {

	// 定义一个要生成的表名(会一直更换表)
	private static final String[] TABLE_NAMES = {"user"};

	public static void main(String[] args) {

		// 获取数据源信息 - 利用Hutool的YamlUtil工具
		Dict dict = YamlUtil.loadByPath("application.yml");
		Map<String, Object> dataSourceConfig = dict.getByPath("spring.datasource");
		String url = String.valueOf(dataSourceConfig.get("url"));
		String username = String.valueOf(dataSourceConfig.get("username"));
		String password = String.valueOf(dataSourceConfig.get("password"));

		//配置数据源
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);

		//创建配置内容，两种风格都可以。
		//GlobalConfig globalConfig = createGlobalConfigUseStyle1();
		GlobalConfig globalConfig = createGlobalConfigUseStyle2();

		//通过 datasource 和 globalConfig 创建代码生成器
		Generator generator = new Generator(dataSource, globalConfig);

		//生成代码
		generator.generate();
	}

	/**
	 * 分句调用，不直观！
	 * @return
	 */
    /*public static GlobalConfig createGlobalConfigUseStyle1() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.setBasePackage("com.test");

        //设置表前缀和只生成哪些表
        globalConfig.setTablePrefix("tb_");
        globalConfig.setGenerateTable("tb_account", "tb_account_session");

        //设置生成 entity 并启用 Lombok
        globalConfig.setEntityGenerateEnable(true);
        globalConfig.setEntityWithLombok(true);
        //设置项目的JDK版本，项目的JDK为14及以上时建议设置该项，小于14则可以不设置
        globalConfig.setEntityJdkVersion(17);

        //设置生成 mapper
        globalConfig.setMapperGenerateEnable(true);

        //可以单独配置某个列
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setColumnName("tenant_id");
        columnConfig.setLarge(true);
        columnConfig.setVersion(true);
        globalConfig.setColumnConfig("tb_account", columnConfig);

        return globalConfig;
    }*/


	/**
	 * 链式调用，好用直观
	 *
	 * @return
	 */
	public static GlobalConfig createGlobalConfigUseStyle2() {
		// 创建配置内容
		GlobalConfig globalConfig = new GlobalConfig();

		// 设置根包，建议先生成至一个临时目录下，再移动到对应的项目目录
		globalConfig.getPackageConfig()
				.setBasePackage("com.yupi.yucodemotherbackend.genresult");

		// 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
		globalConfig.getStrategyConfig()
				.setGenerateTable(TABLE_NAMES)
				// 设置逻辑删除默认字段名称
				.setLogicDeleteColumn("isDelete");

		// 设置生成 entity 并启用 Lombok
		globalConfig.enableEntity()
				.setWithLombok(true)
				.setJdkVersion(21);

		// 设置生成 mapper、mapper.xml
		globalConfig.enableMapper();
		globalConfig.enableMapperXml();

		// 设置生成 Service、ServiceImpl
		globalConfig.enableService();
		globalConfig.enableServiceImpl();

		// 设置生成 Controller
		globalConfig.enableController();

		// 设置生成的 默认注释 -> 如设置生成的时间和作者
		globalConfig.getJavadocConfig()
				.setAuthor("<a href=\"https://github.com/Karry178\">程序员Karry178</a>")
				.setSince("");

		return globalConfig;
	}
}