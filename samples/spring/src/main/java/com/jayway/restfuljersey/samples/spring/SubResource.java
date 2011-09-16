package com.jayway.restfuljersey.samples.spring;

import com.jayway.jersey.rest.resource.Resource;

public class SubResource implements Resource {
	public void sayHello(String to) {
		System.out.println("hello: " + to);
	}
}
