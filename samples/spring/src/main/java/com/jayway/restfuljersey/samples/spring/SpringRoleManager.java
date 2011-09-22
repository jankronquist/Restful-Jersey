package com.jayway.restfuljersey.samples.spring;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class SpringRoleManager {
	public <T> void addRole(Class<T> clazz, T object) {
		RequestContextHolder.currentRequestAttributes().setAttribute(clazz.getName(), object, RequestAttributes.SCOPE_REQUEST);
	}
	@SuppressWarnings("unchecked")
	public <T> T getRole(Class<T> clazz) {
		Object instance = RequestContextHolder.currentRequestAttributes().getAttribute(clazz.getName(), RequestAttributes.SCOPE_REQUEST);
        if ( instance == null ) return null;
        if ( clazz.isAssignableFrom( instance.getClass() ) ) {
            return (T) instance;
        }
        // maybe throw exception. Inconsistent
        return null;
	}
}
