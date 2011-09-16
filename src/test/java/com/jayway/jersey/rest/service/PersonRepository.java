package com.jayway.jersey.rest.service;

import java.util.HashMap;
import java.util.Map;

public class PersonRepository {
	
	private Map<String, Person> persons;

	public PersonRepository() {
		this.persons = new HashMap<String, Person>();
		persons.put("1", new Person("Jan", "Kronquist"));
		persons.put("2", new Person("John", "Doe"));
	}
	
	public Person findPerson(String id) {
		Person person = persons.get(id);
		if (person == null) throw new IllegalArgumentException("Person not found: " + id);
		return person;
	}
}
