package com.jayway.jersey.rest.resource;

import com.jayway.jersey.rest.dto.StringDTO;

/**
 * Base class for resources that has named sub resources.
 * E.g. suppose there is a PersonsResource which has a
 * PersonResource as sub resource. The URL pattern would be
 * /persons/12345/. For the PersonResource with id <i>12345</i>
 * to be identified as a sub resource the PersonsResource has
 * to extend NamedResource.
 *
 */
public interface IdResource extends Resource {

    Resource id( StringDTO id);

}
