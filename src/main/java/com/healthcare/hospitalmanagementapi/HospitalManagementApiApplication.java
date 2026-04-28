package com.healthcare.hospitalmanagementapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HospitalManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HospitalManagementApiApplication.class, args);
	}

}
