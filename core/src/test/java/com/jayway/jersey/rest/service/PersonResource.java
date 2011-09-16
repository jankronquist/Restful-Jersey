package com.jayway.jersey.rest.service;

import com.jayway.jersey.rest.resource.IndexResource;
import com.jayway.jersey.rest.resource.Resource;

public class PersonResource implements Resource, IndexResource {
	
	private final Person person;

	public PersonResource(Person person) {
		this.person = person;
	}
	
	@Override
	public Object index() {
		String result = person.getFirstName() + " " + person.getLastName();
		System.out.println("index is: " + result);
		return result;
	}
	
	public RootResource root() {
		return null;
	}

}
