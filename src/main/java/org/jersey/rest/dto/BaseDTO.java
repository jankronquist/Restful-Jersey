package org.jersey.rest.dto;

/**
 * All DTO classes must implement this interface for the 
 * framework to be able to automatically marshal 
 * and unmarshal the objects. 
 * 
 * Resource methods must only have methods that accept 
 * or return sub classes of BaseDTO - otherwise they 
 * will be ingored by the framework 
 * 
 * @author Mads Br�gger Enevoldsen
 *
 */
public interface BaseDTO {

}
