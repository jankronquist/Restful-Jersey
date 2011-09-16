package com.jayway.jersey.rest.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.jersey.rest.dto.StringDTO;
import com.jayway.jersey.rest.resource.IdResource;
import com.jayway.jersey.rest.resource.Resource;

public class PersonsResource implements IdResource {
	
	@Autowired
	private PersonRepository personRepository;

	@Override
	public Resource id(StringDTO id) {
		System.out.println("hello from PersonsResource! id=" + id.string());
		return new PersonResource(personRepository.findPerson(id.string()));
	}

}
