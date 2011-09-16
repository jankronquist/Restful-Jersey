package com.jayway.jersey.rest.service;

import javax.ws.rs.Path;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.jayway.jersey.rest.SpringRestfulJerseyService;
import com.jayway.jersey.rest.resource.HtmlHelper;

/**
 */
@Path("test")
public class RestService extends SpringRestfulJerseyService {
	
	public RestService() {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ServiceConfiguration.class);
    	applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    	setApplicationContext(applicationContext);
	}

    @Override
    protected void setupContext() {
        if ( StateHolder.get() != null && StateHolder.get() instanceof HtmlHelper ) {
            getContextMap().put( HtmlHelper.class, (HtmlHelper) StateHolder.get());
        }
    }

}
