package com.jayway.jersey.rest.resource;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ResourceMethod {
    private MethodType type = MethodType.ILLEGAL;
    private final Method method;
    private final String name;
    private final boolean isIndex;

    public static ResourceMethod make( Method method ) {
        if ( Modifier.isAbstract( method.getModifiers()) ) return null;
        if ( !Modifier.isPublic( method.getModifiers())) return null;
        if ( !ResourceUtil.checkConstraint(method) ) return null;
        return new ResourceMethod(method);
    }

    private ResourceMethod( Method method ) {
        this.method = method;
        this.name = method.getName();

        boolean index = false;
        if ( argumentCheck( method.getParameterTypes() ) ) {
        	Class<?> returnType = method.getReturnType();
            if ( returnType.equals( Void.TYPE ) ) {
                type = MethodType.COMMAND;
            } else if ( Resource.class.isAssignableFrom( returnType ) ) {
                if ( getMethod().getParameterTypes().length == 0 ) type = MethodType.SUBRESOURCE;
                else type = MethodType.ILLEGAL;
            } else {
                type = MethodType.QUERY;
                if ( name.equals( "index" ) ){
                    index = true;
                }
            }
        }
        this.isIndex = index;
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

	public Method getMethod() {
		return method;
	}
}