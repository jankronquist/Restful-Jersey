package com.jayway.restfuljersey.samples.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jayway.jersey.rest.resource.Resource;

@Configuration
public class SampleConfiguration {
	
	public SampleConfiguration() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>");
		System.out.println("Hello from sample config!!!");
	}

	@Bean
	public Resource root() {
		return new RootResource();
	}
	
	@Bean
	public Object service() {
		return new SampleService(); 
	}
}
