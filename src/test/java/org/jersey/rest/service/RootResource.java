package org.jersey.rest.service;

import org.jersey.rest.dto.IntegerDTO;
import org.jersey.rest.dto.StringDTO;
import org.jersey.rest.resource.Resource;

/**
 */
public class RootResource extends Resource {

    public RootResource sub() {
        return new RootResource();
    }

    public void command( StringDTO input ) {
        StateHolder.set(input);
    }

    public IntegerDTO addten( IntegerDTO number ) {
        number.setInteger( number.getInteger() + 10 );
        return number;
    }

    public StringDTO echo( StringDTO input ) {
        return input;
    }
    
    public OtherResource other() {
        return new OtherResource();
    }

    public void wrong( String input ) {

    }

    public void wrong2( StringDTO one, StringDTO two ) {
        
    }
}
