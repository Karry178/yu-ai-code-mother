package com.yupi.yucodemotherbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yupi.yucodemotherbackend.mapper")
public class YuCodeMotherBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(YuCodeMotherBackendApplication.class, args);
	}

}
