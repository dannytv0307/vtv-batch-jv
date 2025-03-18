package com.example.batch_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.example.batch_demo.entity")
@EnableJpaRepositories("com.example.batch_demo.repository")
@EnableBatchProcessing
@EnableScheduling
@ComponentScan(basePackages = "com.example.batch_demo")
public class BatchDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(BatchDemoApplication.class, args);
	}
}
