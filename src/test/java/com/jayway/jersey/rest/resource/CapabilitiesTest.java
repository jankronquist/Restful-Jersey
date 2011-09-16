package com.jayway.jersey.rest.resource;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.jayway.jersey.rest.service.AbstractRunner;
import com.jayway.jersey.rest.service.StateHolder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 */
public class CapabilitiesTest extends AbstractRunner {
    
    public CapabilitiesTest() throws Exception {
        super();
    }

    @Test
    public void testRoot() {
        String response = webResource.path("test/").accept(MediaType.TEXT_HTML).get(String.class);
        System.out.println( response );
    }

    @Test
    public void testPathEvaluation() {
        String root = webResource.path("test/").type(MediaType.TEXT_HTML).get(String.class);
        String subsub = webResource.path("test/sub/sub/").type(MediaType.TEXT_HTML).get(String.class);
        Assert.assertEquals( root, subsub );
    }

    @Test
    public void discover() {
        String result = webResource.path("test/").type(MediaType.TEXT_HTML).get(String.class);
        hasMethod(result, "command");
        hasMethod(result, "sub");
        hasMethod(result, "other" );
        hasMethod(result, "addten");
        hasMethod(result, "echo");
    }

    private void hasMethod( String result, String name ) {
        Assert.assertTrue("Must contain "+name , result.contains( name ));
    }

    @Test
    public void testPathEvaluationWrong() {
        try {
            webResource.path("test/sub/sub2/").type(MediaType.TEXT_HTML).get(String.class);
            Assert.fail( "must throw Not Found" );
        } catch( UniformInterfaceException e) {
            Assert.assertEquals( 404, e.getResponse().getStatus() );
        }

    }

    @Test
    public void testUnsupportedMediaType() {
        try {
            webResource.path("test/").type( MediaType.APPLICATION_OCTET_STREAM ).get( String.class );
            Assert.fail( "Must throw unsupported media type" );
        } catch( UniformInterfaceException e) {
            Assert.assertEquals( 415, e.getResponse().getStatus() );
        }
    }

    @Test
    public void testMediaTypes() {
        webResource.path("test/sub/").type( MediaType.APPLICATION_XML ).get( String.class );
        webResource.path("test/sub/").type( MediaType.APPLICATION_JSON ).get( String.class );
        webResource.path("test/sub/").type( MediaType.TEXT_HTML ).get( String.class );
    }


}
