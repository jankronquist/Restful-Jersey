package com.jayway.restfuljersey.samples.spring;

import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

import com.jayway.jersey.rest.SpringRestfulJerseyService;

@Path("root")
@Component
public class SampleService extends SpringRestfulJerseyService {
}
