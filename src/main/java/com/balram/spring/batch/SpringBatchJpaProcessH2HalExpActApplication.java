package com.balram.spring.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class SpringBatchJpaProcessH2HalExpActApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchJpaProcessH2HalExpActApplication.class, args);
	}

}
