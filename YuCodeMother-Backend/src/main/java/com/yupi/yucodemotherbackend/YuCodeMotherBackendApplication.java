package com.yupi.yucodemotherbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.yupi.yucodemotherbackend.mapper")
public class YuCodeMotherBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(YuCodeMotherBackendApplication.class, args);
	}

}
