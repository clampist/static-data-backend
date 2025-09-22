package com.staticdata.platform;

import org.springframework.boot.SpringApplication;

public class TestStaticDataPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.from(StaticDataPlatformApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
