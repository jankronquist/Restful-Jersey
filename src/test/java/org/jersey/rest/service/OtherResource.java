package org.jersey.rest.service;

import org.jersey.rest.dto.StringDTO;
import org.jersey.rest.resource.DeletableResource;
import org.jersey.rest.resource.IdResource;
import org.jersey.rest.resource.IndexResource;
import org.jersey.rest.resource.Resource;

/**
 */
public class OtherResource extends Resource implements IdResource, IndexResource, DeletableResource {

    @Override
    public Resource id(StringDTO id) {
        return new RootResource();
    }


    @Override
    public StringDTO index() {
        StateHolder.set( new StringDTO((String) StateHolder.get()) );
        return (StringDTO) StateHolder.get();
    }

    @Override
    public void delete() {
        StateHolder.set("Delete invoked");
    }
}
