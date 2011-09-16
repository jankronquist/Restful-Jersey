package com.jayway.jersey.rest.reflection;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;

import com.jayway.jersey.rest.RestfulJerseyService;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONUnmarshaller;

public class Parameter {
	
	private final String name;
	private final Class<?> type;

	public Parameter(Class<?> type) {
		this(type.getSimpleName(), type);
	}

	public Parameter(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Iterable<Parameter> getChildParameters() {
		List<Parameter> children = new LinkedList<Parameter>();
		for (Field f : type.getDeclaredFields()) {
	        if ( Modifier.isFinal(f.getModifiers())) continue;
			children.add(new Parameter(f.getName(), f.getType()));
		}
		return Collections.unmodifiableCollection(children);
	}

	public String getPath() {
		return name;
	}

	public boolean isTextField() {
		return RestfulJerseyService.basicTypes.contains(  type ); 
	}

	public boolean isPassword() {
		return name.equals("password");
	}

	public boolean hasOptions() {
		return type.isEnum();
	}

	public Iterable<?> getOptions() {
		return Arrays.asList(type.getEnumConstants());
	}

	public Object[] unmarshall(String contentType, InputStream xml) throws Exception {
        if ( MediaType.APPLICATION_JSON.equals(contentType)) {
            Class<?> dtoClass = type.newInstance().getClass();
            JAXBContext context = JSONJAXBContext.newInstance( dtoClass );
            JSONUnmarshaller jsonUnmarshaller = JSONJAXBContext.getJSONUnmarshaller( context.createUnmarshaller() );
            return new Object[]{ jsonUnmarshaller.unmarshalFromJSON( xml, dtoClass ) };
        } else {
            // default to xml
            JAXBContext context = JAXBContext.newInstance(type.newInstance().getClass());
            return new Object[] { context.createUnmarshaller().unmarshal( xml ) };
        }
	}

}
