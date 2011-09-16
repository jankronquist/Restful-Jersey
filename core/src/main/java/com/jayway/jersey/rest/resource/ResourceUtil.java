package com.jayway.jersey.rest.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.jayway.jersey.rest.RestfulJerseyService;
import com.jayway.jersey.rest.constraint.Constraint;
import com.jayway.jersey.rest.constraint.ConstraintEvaluator;
import com.jayway.jersey.rest.dto.StringDTO;
import com.jayway.jersey.rest.reflection.Capability;

public class ResourceUtil {

	public static boolean checkConstraint(Method method) {
	    for ( Annotation a : method.getAnnotations() ) {
	        if ( a.annotationType().getAnnotation(Constraint.class) != null ) {
	            return constraintEvaluator( a );
	        }
	    }
	    return true;
	}

	private static boolean constraintEvaluator( Annotation annotation ) {
	    if ( annotation == null ) return true;
	    Constraint constraint = annotation.annotationType().getAnnotation(Constraint.class);
	    try {
	        ConstraintEvaluator<Annotation, ContextMap> constraintEvaluator = constraint.value().newInstance();
	        return constraintEvaluator.isValid( annotation, RestfulJerseyService.getContextMap());
	
	    } catch (InstantiationException e) {
	        e.printStackTrace();
	    } catch (IllegalAccessException e) {
	        e.printStackTrace();
	    }
	    return true;
	}

	public static Resource invokePathMethod(Object resource,  String path) {
	    try {
	        Capability method = findMethod(resource, path);
	        if ( method.isSubResource() ) {
	            try {
	                return (Resource) method.get(resource);
	            } catch ( IllegalAccessException e) {
	                throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
	            } catch ( InvocationTargetException e) {
	                if ( e.getCause() instanceof RuntimeException ) {
	                    throw (RuntimeException) e.getCause();
	                }
	                throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
	            }
	        }
	    } catch ( WebApplicationException notFound ) {
	        if (resource instanceof IdResource ) {
	            return ((IdResource) resource).id( new StringDTO( path ) );
	        }
	    }
	    throw new WebApplicationException( Response.Status.NOT_FOUND );
	}

	public static <T> T role(Class<T> clazz) {
	    ContextMap map = RestfulJerseyService.getContextMap();
	    return map.get(clazz);
	}

	public static <T> void addRole( Class<T> clazz, T instance ) {
	    ContextMap map = RestfulJerseyService.getContextMap();
	    map.put(clazz, instance);
	}

	public static Capability findMethod(Object resource, String name) {
	    Class<?> clazz = resource.getClass();
	    for ( Method method : clazz.getDeclaredMethods() ) {
	        if ( method.getName().equals( name ) ) {
	            Capability resourceMethod = Capability.make( method );
	            if ( resourceMethod != null && !resourceMethod.isIllegal() ) return resourceMethod;
	        }
	    }
	    throw new WebApplicationException( Response.Status.NOT_FOUND );
	}

	public static void invokeDelete(Object resource) {
	    if ( resource instanceof DeletableResource) {
	        (( DeletableResource) resource).delete();
	    } else {
	        throw new WebApplicationException( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
	    }
	}
}
