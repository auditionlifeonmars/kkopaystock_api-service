package kr.co.apiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;

@EnableDiscoveryClient
@SpringBootApplication
public class ApiServiceApplication {
	
	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("messages/messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	public static void main(String[] args) { SpringApplication.run(ApiServiceApplication.class, args);}

}
	
