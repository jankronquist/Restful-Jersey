package com.jayway.jersey.rest.resource;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.jayway.jersey.rest.service.AbstractRunner;

/**
 */
public class IdTest extends AbstractRunner {

    public IdTest() throws Exception {
		super();
	}

	@Test
    public void testDiscoverId() {
        String person = webResource.path("test/persons/1/").type(MediaType.TEXT_HTML).get(String.class);
        System.out.println(person);
    }
}
