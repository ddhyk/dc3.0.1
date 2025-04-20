package org.example.dcdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("org.example.dcdemo.model")
@EnableJpaRepositories("org.example.dcdemo.repository")
public class DcDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DcDemoApplication.class, args);
	}

}
