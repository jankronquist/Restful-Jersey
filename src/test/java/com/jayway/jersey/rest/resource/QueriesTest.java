package com.jayway.jersey.rest.resource;

import com.jayway.jersey.rest.dto.IntegerDTO;
import com.jayway.jersey.rest.dto.StringDTO;
import com.jayway.jersey.rest.service.AbstractRunner;
import com.jayway.jersey.rest.service.StateHolder;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

/**
 */
public class QueriesTest extends AbstractRunner {

    public QueriesTest() throws Exception {
        super( );
    }

     @Test
     public void testEchoMethod() {
         StringDTO response = webResource.path("test/echo").queryParam("StringDTO.string", "echo")
                 .type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get(StringDTO.class);

         Assert.assertEquals( "echo", response.string() );
     }


    @Test
    public void testEchoMethodXml() {
        StringDTO response = webResource.path("test/echo").queryParam("StringDTO.string", "echo")
                .type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).get(StringDTO.class);

        Assert.assertEquals( "echo", response.string() );
    }

    @Test
    public void testInvokeIndex() {
        String expected = "Expected String";
        StateHolder.set( expected );
        webResource.path("test/other/index").accept( MediaType.APPLICATION_JSON ).get( StringDTO.class );
        StringDTO result = (StringDTO) StateHolder.get();
        Assert.assertEquals( expected, result.string() );
    }


    @Test
    public void testQueryWithInteger() {
        IntegerDTO dto = webResource.path("test/addten").queryParam("IntegerDTO.integer", "60")
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).get(IntegerDTO.class);
        Assert.assertEquals( 70, dto.getInteger().intValue() );
    }

    @Test
    public void testQueryWithIntegerWrongInput() {
        mustThrow(webResource.path("test/addten").queryParam("IntegerDTO.integer", "x6f?0")
                .accept(MediaType.APPLICATION_JSON), "GET", IntegerDTO.class, 400);
    }

}
