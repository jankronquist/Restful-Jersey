package com.jayway.restfuljersey.samples.spring;

import com.jayway.jersey.rest.resource.Resource;

public class RootResource implements Resource {
	public void doStuff() {
		System.out.println("DO STUFF!!!");
	}
}
