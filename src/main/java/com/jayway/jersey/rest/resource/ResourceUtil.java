package com.jayway.jersey.rest.resource;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;

import com.jayway.jersey.rest.RestfulJerseyService;
import com.jayway.jersey.rest.constraint.Constraint;
import com.jayway.jersey.rest.constraint.ConstraintEvaluator;
import com.jayway.jersey.rest.dto.StringDTO;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONUnmarshaller;

public class ResourceUtil {

	static class ResourceMethod {
	    private MethodType type = MethodType.ILLEGAL;
	    private Method method;
	    private String name;
	    private boolean isIndex;
	
	    public ResourceMethod( Method method ) {
	        this.method = method;
	        this.name = method.getName();
	
	        if ( Modifier.isAbstract( method.getModifiers()) ) return;
	        if ( !Modifier.isPublic( method.getModifiers())) return;
	        if ( !checkConstraint(method) ) return;
	
	        if ( argumentCheck( method.getParameterTypes() ) ) {
	            handleReturnType( method.getReturnType() );
	        }
	    }
	
	    private boolean argumentCheck( Class<?>[] parameterTypes ) {
	        return parameterTypes.length <= 1;
	    }
	
	    private void handleReturnType( Class<?> returnType ) {
	        if ( returnType.equals( Void.TYPE ) ) {
	            type = MethodType.COMMAND;
	        } else if ( Resource.class.isAssignableFrom( returnType ) ) {
	            if ( method.getParameterTypes().length == 0 ) type = MethodType.SUBRESOURCE;
	            else type = MethodType.ILLEGAL;
	        } else {
	            type = MethodType.QUERY;
	            if ( name.equals( "index" ) ){
	                isIndex = true;
	            }
	        }
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
	}

	static enum MethodType {
	    COMMAND, QUERY, SUBRESOURCE, ILLEGAL
	}

	/**
	 * lists the capabilities of this resource.
	 *
	 * @return string containing html
	 */
	public static String capabilities(Object resource) {
	    List<ResourceMethod> methods = new ArrayList<ResourceMethod>();
	
	    StringBuilder sb = new StringBuilder();
	    Class<?> clazz = resource.getClass();
	    sb.append( "<h1>" ).append( clazz.getName() ).append("</h1>");
	    for ( Method method : clazz.getDeclaredMethods() ) {
	        if ( method.isSynthetic() ) continue;
	        methods.add(new ResourceMethod(method));
	    }
	    role( HtmlHelper.class).addResourceMethods(sb, methods);
	
	    sb.append("<h2>Index</h2>");
	    if (resource instanceof IndexResource ) {
	        sb.append(((IndexResource) resource).index());
	    }
	    return sb.toString();
	}

	public static Response post(Object resource, String method, MultivaluedMap<String,String> formParams, InputStream stream) {
	    ResourceMethod m = findMethod(resource, method);
	    if ( !m.isCommand() ) throw new WebApplicationException( HttpServletResponse.SC_NOT_FOUND );
	
	    Object[] arguments = stream == null ? arguments(m.method, formParams ) : arguments( m.method, stream );
	    return invokeCommand(m.method, resource, arguments );
	}

	static Object[] arguments( Method m, InputStream xml ) {
	    try {
	        String contentType = role( HttpServletRequest.class ).getContentType();
	        if ( m.getParameterTypes().length == 1 ) {
	            Class<?> dto = m.getParameterTypes()[0];
	            if ( contentType.equals( MediaType.APPLICATION_JSON )) {
	                Class<?> dtoClass = dto.newInstance().getClass();
	                JAXBContext context = JSONJAXBContext.newInstance( dtoClass );
	                JSONUnmarshaller jsonUnmarshaller = JSONJAXBContext.getJSONUnmarshaller( context.createUnmarshaller() );
	                return new Object[]{ jsonUnmarshaller.unmarshalFromJSON( xml, dtoClass ) };
	            } else {
	                // default to xml
	                JAXBContext context = JAXBContext.newInstance(dto.newInstance().getClass());
	                return new Object[] { context.createUnmarshaller().unmarshal( xml ) };
	            }
	        }
	        return new Object[0];
	    } catch (Exception e) {
	        throw new WebApplicationException( e, Response.Status.BAD_REQUEST );
	    }
	}

	static Object[] arguments(Method m, MultivaluedMap<String, String> formParams ) {
	    if ( m.getParameterTypes().length == 1 ) {
	        Class<?> dto = m.getParameterTypes()[0];
	        return new Object[]{ populateDTO( dto, formParams, dto.getSimpleName() ) };
	    }
	    return new Object[0];
	}

	static boolean checkConstraint(Method method) {
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
	        ResourceMethod method = findMethod(resource, path);
	        if ( method.isSubResource() ) {
	            try {
	                return (Resource) method.method.invoke(resource);
	            } catch ( IllegalAccessException e) {
	                e.printStackTrace();
	                throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
	            } catch ( InvocationTargetException e) {
	                if ( e.getCause() instanceof RuntimeException ) {
	                    throw (RuntimeException) e.getCause();
	                }
	                e.printStackTrace();
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

	private static ResourceMethod findMethod(Object resource, String name) {
	    Class<?> clazz = resource.getClass();
	    for ( Method method : clazz.getDeclaredMethods() ) {
	        if ( method.getName().equals( name ) ) {
	            ResourceMethod resourceMethod = new ResourceMethod( method );
	            if ( !resourceMethod.isIllegal() ) return resourceMethod;
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

	private static Response invokeCommand(Method method, Object instance, Object... arguments) {
	    try {
	        method.invoke( instance, arguments );
	        return successResponse();
	    } catch (InvocationTargetException e) {
	        if ( e.getCause() instanceof WebApplicationException ) throw (WebApplicationException) e.getCause();
	        throw new WebApplicationException( e.getCause(), Response.Status.BAD_REQUEST );
	    } catch (IllegalAccessException e) {
	        e.printStackTrace();
	        throw new WebApplicationException( e.getCause(), Response.Status.INTERNAL_SERVER_ERROR );
	    }
	}

	private static Response successResponse() {
	    return Response.ok( "Operation Completed Successfully" ).status(200).build();
	}

	public static Object get(Object resource, String get ) {
	    ResourceMethod m = findMethod(resource, get);
	    if ( m.isSubResource() ) {
	        throw new WebApplicationException( Response.Status.NOT_FOUND );
	    }
	
	    if ( m.isCommand() ) {
	        Response response = Response.status( HttpServletResponse.SC_METHOD_NOT_ALLOWED).entity( new HtmlHelper().createForm( m.method, HttpMethod.POST ) ).build();
	        throw new WebApplicationException( response );
	    }
	
	    MultivaluedMap<String, String> queryParams = role(UriInfo.class).getQueryParameters();
	    if ( queryParams.size() == 0 && m.method.getParameterTypes().length > 0) {
	        return new HtmlHelper().createForm(m.method, HttpMethod.GET );
	    } else {
	        try {
	            Object result;
	            if ( m.method.getParameterTypes().length == 1) {
	                Class<?> dto = m.method.getParameterTypes()[0];
	                result = m.method.invoke(resource, populateDTO( dto, queryParams, dto.getSimpleName() ) );
	            } else {
	                result = m.method.invoke(resource);
	            }
	            if ( role(HttpServletRequest.class).getHeader("Accept").contains(MediaType.TEXT_HTML) ) {
	                return result.toString();
	            }
	            return result;
	        } catch ( IllegalAccessException e) {
	            throw new WebApplicationException( e, Response.Status.INTERNAL_SERVER_ERROR );
	        } catch ( InvocationTargetException e) {
	            throw new WebApplicationException( e, Response.Status.INTERNAL_SERVER_ERROR );
	        }
	    }
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
