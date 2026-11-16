package com.cfs.Search_Services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SearchServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SearchServicesApplication.class, args);
	}

}
