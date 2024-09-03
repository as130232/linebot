package com.eachnow.linebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
@EnableScheduling
public class LinebotApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(LinebotApplication.class, args);
	}

	@Autowired
	private RequestMappingHandlerMapping handlerMapping;
	@Override
	public void run(String... args) throws Exception {
		handlerMapping.getHandlerMethods().forEach((key, value) -> {
			System.out.println(key + " -> " + value);
		});
	}
}
