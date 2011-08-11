package org.jersey.rest.service;

import org.jersey.rest.RestfulJerseyService;
import org.jersey.rest.resource.HtmlHelper;
import org.jersey.rest.resource.Resource;

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
