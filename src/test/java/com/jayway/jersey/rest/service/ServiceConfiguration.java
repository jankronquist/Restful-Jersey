package com.jayway.jersey.rest.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jayway.jersey.rest.resource.Resource;

@Configuration
public class ServiceConfiguration {

	@Bean
	public Resource root() {
		return new RootResource();
	}

	@Bean
	public PersonRepository personRepository() {
		 return new PersonRepository();
	}
}
