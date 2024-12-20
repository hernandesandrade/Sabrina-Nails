package com.online.commerce;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommerceApplication {

	@Value("${BD_PASSWORD}")
	private static String dbPassword;

	public static void main(String[] args) {
		System.out.println(dbPassword);
		SpringApplication.run(CommerceApplication.class, args);
	}

}
