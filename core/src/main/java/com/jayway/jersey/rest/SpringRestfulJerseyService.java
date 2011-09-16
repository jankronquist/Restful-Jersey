package com.jayway.jersey.rest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.jayway.jersey.rest.reflection.RestReflection;
import com.jayway.jersey.rest.resource.Resource;

/**
 * Extend this class to define your rest service based on Spring.
 */
public class SpringRestfulJerseyService extends RestfulJerseyService implements ApplicationContextAware {
	
	@Autowired
	private Resource root;
	
	private ApplicationContext applicationContext;

    protected Resource root() {
    	return root;
    }
    
    protected void setupContext() {
    }

    
    protected Resource postCreate(Resource resource) {
    	applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
    	return resource;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
