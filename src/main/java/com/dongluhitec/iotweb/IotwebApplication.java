package com.dongluhitec.iotweb;

import com.dongluhitec.iotweb.config.CrosFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class IotwebApplication {

	public static void main(String[] args) {
		SpringApplication.run(IotwebApplication.class, args);
	}

	@Bean
	public FilterRegistrationBean croFilter() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new CrosFilter());
		registration.addUrlPatterns("/*");
		registration.setName("croFilter");
		registration.setOrder(1);
		return registration;
	}

}
