package com.jayway.jersey.rest.service;

import com.jayway.jersey.rest.RestfulJerseyService;
import com.jayway.jersey.rest.resource.HtmlHelper;
import com.jayway.jersey.rest.resource.Resource;

import javax.ws.rs.Path;

/**
 */
@Path("test")
public class RestService extends RestfulJerseyService {

    @Override
    protected void setupContext() {
        if ( StateHolder.get() != null && StateHolder.get() instanceof HtmlHelper ) {
            getContextMap().put( HtmlHelper.class, (HtmlHelper) StateHolder.get());
        }
    }

    @Override
    protected Resource root() {
        return new RootResource();
    }

}
