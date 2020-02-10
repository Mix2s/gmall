package com.hui.gmall.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class GmallManageWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManageWebApplication.class, args);
	}

}
