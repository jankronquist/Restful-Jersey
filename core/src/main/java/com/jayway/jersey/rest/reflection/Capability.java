package com.jayway.jersey.rest.reflection;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.jayway.jersey.rest.resource.MethodType;
import com.jayway.jersey.rest.resource.Resource;
import com.jayway.jersey.rest.resource.ResourceUtil;


public class Capability {
    private MethodType type = MethodType.ILLEGAL;
    private final Method method;
    private final String name;
    private final boolean isIndex;
	private List<Parameter> parameters;

    public static Capability make( Method method ) {
        if ( Modifier.isAbstract( method.getModifiers()) ) return null;
        if ( !Modifier.isPublic( method.getModifiers())) return null;
        if ( !ResourceUtil.checkConstraint(method) ) return null;
        return new Capability(method);
    }

    private Capability( Method method ) {
        this.method = method;
        this.name = method.getName();

        boolean index = false;
        if ( argumentCheck( method.getParameterTypes() ) ) {
        	Class<?> returnType = method.getReturnType();
            if ( returnType.equals( Void.TYPE ) ) {
                type = MethodType.COMMAND;
            } else if ( Resource.class.isAssignableFrom( returnType ) ) {
                if ( method.getParameterTypes().length == 0 ) type = MethodType.SUBRESOURCE;
                else type = MethodType.ILLEGAL;
            } else {
                type = MethodType.QUERY;
                if ( name.equals( "index" ) ){
                    index = true;
                }
            }
        }
        this.isIndex = index;
		List<Parameter> parameters = new LinkedList<Parameter>();
		for (Class<?> type : method.getParameterTypes()) {
			parameters.add(new Parameter(type));
		}
		this.parameters = Collections.unmodifiableList(parameters);

    }

    private boolean argumentCheck( Class<?>[] parameterTypes ) {
        return parameterTypes.length <= 1;
    }

    public MethodType type() {
        return type;
    }
    public String name() {
        return name;
    }

    public boolean isCommand() {
        return type == MethodType.COMMAND;
    }
    public boolean isQuery() {
        return type == MethodType.QUERY;
    }
    public boolean isSubResource() {
        return type == MethodType.SUBRESOURCE;
    }
    public boolean isIllegal() {
        return type == MethodType.ILLEGAL;
    }
    public boolean isIndex() {
        return isIndex;
    }
	public List<Parameter> getParameters() {
		return parameters;
	}

	public int getParameterCount() {
		return method.getParameterTypes().length;
	}

	public Object get(Object resource) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return method.invoke(resource);
	}

	public Object get(Object resource, MultivaluedMap<String, String> queryParams) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return method.invoke(resource, arguments(queryParams));
	}

	public void post(Object resource, String contentType, MultivaluedMap<String, String> formParams, InputStream stream) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
	    Object[] arguments = stream == null ? arguments(formParams ) : arguments( contentType, stream );
	    method.invoke(resource, arguments);
	}

	private Object[] arguments(String contentType, InputStream xml ) {
	    try {
	    	// TODO: we should use the list of mediatypes instead
	        if ( getParameterCount() == 1 ) {
	            Parameter parameter = getParameters().get(0);
	            return parameter.unmarshall(contentType, xml);
	        }
	        return new Object[0];
	    } catch (Exception e) {
	        throw new WebApplicationException( e, Response.Status.BAD_REQUEST );
	    }
	}

	Object[] arguments(MultivaluedMap<String, String> params ) {
	    if ( getParameterCount() == 1 ) {
	        Class<?> dto = method.getParameterTypes()[0];
	        return new Object[]{ populateDTO( dto, params, dto.getSimpleName() ) };
	    }
	    return new Object[0];
	}

	private static Object populateDTO(Class<?> dto, MultivaluedMap<String, String> formParams, String prefix ) {
	    try {
	        Object o = dto.newInstance();
	        for ( Field f : o.getClass().getDeclaredFields() ) {
	            if ( Modifier.isFinal( f.getModifiers() ) ) continue;
	            f.setAccessible(true);
	            String value = formParams.getFirst(prefix + "." + f.getName());
	
	            if ( f.getType() == String.class ) {
	                f.set( o, value );
	            } else if ( f.getType() == Integer.class ) {
	                f.set( o, Integer.valueOf( value ) );
	            } else if ( f.getType() == Double.class ) {
	                f.set( o, Double.valueOf( value ) );
	            } else if ( f.getType() == Boolean.class ) {
	                f.set( o, Boolean.valueOf( value ) );
	            } else if ( f.getType().isEnum() ) {
	                f.set( o, Enum.valueOf((Class<Enum>) f.getType(), value));f.set( o, Enum.valueOf((Class<Enum>) f.getType(), value));
	            } else {
	                Object innerDto = populateDTO( f.getType(), formParams, prefix + "." +f.getName() );
	                f.set( o, innerDto );
	            }
	        }
	        return o;
	    } catch (Exception e) {
	        throw new WebApplicationException( e, Response.Status.BAD_REQUEST );
	    }
	}
}