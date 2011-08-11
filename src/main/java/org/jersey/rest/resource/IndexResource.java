package org.jersey.rest.resource;

import org.jersey.rest.dto.BaseDTO;

/**
 * Let the resource class implement this method 
 * to automatically output the index result in the
 * capabilities of the resource
 */
public interface IndexResource {
    BaseDTO index();
}
