package com.jayway.jersey.rest;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.jayway.jersey.rest.reflection.Capabilities;
import com.jayway.jersey.rest.reflection.Capability;
import com.jayway.jersey.rest.reflection.HtmlRestReflection;
import com.jayway.jersey.rest.reflection.JsonRestReflection;
import com.jayway.jersey.rest.reflection.RestReflection;
import com.jayway.jersey.rest.resource.ContextMap;
import com.jayway.jersey.rest.resource.IndexResource;
import com.jayway.jersey.rest.resource.Resource;
import com.jayway.jersey.rest.resource.ResourceUtil;
import com.sun.jersey.api.core.HttpContext;

/**
 * Extend this class to define your rest
 * service
 */
public abstract class RestfulJerseyService {

    protected abstract Resource root();
    protected abstract void setupContext();

    private Map<MediaType, RestReflection> reflectors = new HashMap<MediaType, RestReflection>();
    
    public RestfulJerseyService() {
    	reflectors.put(MediaType.APPLICATION_JSON_TYPE, JsonRestReflection.INSTANCE);
    	reflectors.put(MediaType.TEXT_HTML_TYPE, HtmlRestReflection.INSTANCE);
	}
    
    public void registerRestReflection(MediaType mediaType, RestReflection restReflection) {
    	reflectors.put(mediaType, restReflection);
    }
    
    private RestReflection restReflection() {
    	List<MediaType> mediaTypes = context.getRequest().getAcceptableMediaTypes();
    	for (MediaType mediaType : mediaTypes) {
    		RestReflection restReflection = reflectors.get(mediaType);
    		if (restReflection != null) {
        		return restReflection;
    		}
		}
    	System.out.println("mediaTypes: " + mediaTypes);
		throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
	}

    /**
     * Override this method to initialize resources, for example using dependency injection.
     */
    protected Resource postCreate(Resource resource) {
    	return resource;
	}

    private static ThreadLocal<ContextMap> currentMap = new ThreadLocal<ContextMap>();

    protected static void setContextMap( ContextMap map) {
        currentMap.set( map );
    }

    public static ContextMap getContextMap() {
        return currentMap.get();
    }

    @Context private UriInfo uriInfo;
    @Context private HttpServletResponse response;
    @Context private HttpServletRequest request;
    @Context private HttpContext context;


    public static Set<Class<?>> basicTypes;

    static {
        basicTypes = new HashSet<Class<?>>();
        basicTypes.add( String.class);
        basicTypes.add( Integer.class);
        basicTypes.add( Double.class );
        basicTypes.add( Boolean.class );
    }



    public void setup() {
        ContextMap contextMap = new ContextMap();
        contextMap.put( HttpServletResponse.class, response );
        contextMap.put( HttpServletRequest.class, request );
        contextMap.put( UriInfo.class, uriInfo );
        setContextMap(contextMap);

        // call application specific context setup
        setupContext();
    }

    @GET
    public Object capabilities() {
        setup();
        if ( uriInfo.getPath().endsWith( "/") ) {
            return capabilities(root());
        } else throw new WebApplicationException( Response.Status.NOT_FOUND );
    }

    @GET
    @Path("{get:.*}")
    @Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/html;charset=utf-8" })
    public Object get(  ) {
        setup();
        return evaluateGet(uriInfo.getPath());
    }
    
    @POST
    @Path("{post:.*}")
    @Consumes( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response postJSON( InputStream json ) {
        setup();
        return evaluatePostPut(uriInfo.getPath(), json, null);
    }
    
    @PUT
    @Path("{put:.*}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putJSON( InputStream json ) {
        setup();
        return evaluatePostPut( uriInfo.getPath(), json, null );
    }


    @POST
    @Path("{post:.*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postForm( MultivaluedMap<String, String> formParams ) {
        setup();
        return evaluatePostPut( uriInfo.getPath(), null, formParams );
    }

    @PUT
    @Path("{put:.*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response putForm( MultivaluedMap<String, String> formParams ) {
        setup();
        return evaluatePostPut( uriInfo.getPath(), null, formParams );
    }


    @DELETE
    @Path("{delete:.*}")
    public Response delete() {
        setup();
        evaluateDelete(uriInfo.getPath());
        return Response.ok( "Operation Completed Successfully" ).status(200).build();
    }


    protected Object evaluateGet( String path ) {
        PathAndMethod pathAndMethod = new PathAndMethod( path );
        if ( pathAndMethod.method() == null ) {
            return capabilities(evaluatePath( pathAndMethod.pathSegments()));
        }
        return get(evaluatePath( pathAndMethod.pathSegments()), pathAndMethod.method());
    }

    protected Response evaluatePostPut( String path, InputStream stream, MultivaluedMap<String, String> formParams ) {
        PathAndMethod pathAndMethod = new PathAndMethod( path );
        if ( pathAndMethod.method() == null ) {
            throw new WebApplicationException( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
        }
        return post(evaluatePath( pathAndMethod.pathSegments()), pathAndMethod.method(), formParams, stream);
    }

    protected void evaluateDelete( String path ) {
        PathAndMethod pathAndMethod = new PathAndMethod(path);
        if ( pathAndMethod.method() != null ) {
            throw new WebApplicationException( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
        }
        ResourceUtil.invokeDelete(evaluatePath( pathAndMethod.pathSegments()));
    }

    private Resource evaluatePath( List<String> segments ) {
        Resource current = root();
        for ( String pathSegment: segments ) {
            current = ResourceUtil.invokePathMethod(current, pathSegment);
            postCreate(current);
        }
        return current;
    }

	class PathAndMethod {
        private List<String> pathSegments;
        private String method;

        public PathAndMethod( String rawPath, String method ) {
            this(rawPath);
            if ( this.method == null ) this.method = method;
        }

        public PathAndMethod( String rawPath ) {
            int index = rawPath.indexOf( '/' );
            if ( index > 0 ) {
                rawPath = rawPath.substring( index+1 );
            }
            boolean onlyPathSegments = rawPath.endsWith("/");
            pathSegments =  new ArrayList<String>();
            String[] split = rawPath.split("/");
            for ( int i=0; i<split.length; i++) {
                if ((onlyPathSegments || i != split.length - 1) && split[i].length() > 0) {
                    pathSegments.add(split[i]);
                }
            }
            method = null;
            if (!onlyPathSegments) {
                method = split[ split.length -1 ];
            }
        }

        public List<String> pathSegments() {
            return pathSegments;
        }

        public String method() {
            return method;
        }
    }

	public Object get(Object resource, String get ) {
	    Capability m = ResourceUtil.findMethod(resource, get);
	    if ( m.isSubResource() ) {
	        throw new WebApplicationException( Response.Status.NOT_FOUND );
	    }
	
	    if ( m.isCommand() ) {
	        Response response = Response.status( HttpServletResponse.SC_METHOD_NOT_ALLOWED).entity( restReflection().renderCommandForm( m) ).build();
	        throw new WebApplicationException( response );
	    }
	
	    MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
	    if ( queryParams.size() == 0 && m.getParameterCount() > 0) {
	        return restReflection().renderQueryForm(m);
	    } else {
	        try {
	            Object result = m.get(resource, queryParams);
	            if ( request.getHeader("Accept").contains(MediaType.TEXT_HTML) ) {
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

	/**
	 * lists the capabilities of this resource.
	 */
	private Object capabilities(Object resource) {
	    Class<?> clazz = resource.getClass();
		Capabilities capabilities = new Capabilities(clazz.getName());
	    for ( Method m : clazz.getDeclaredMethods() ) {
	        if ( m.isSynthetic() ) continue;
	    	Capability method = Capability.make(m);
	    	if (method != null) {
		    	switch (method.type()) {
		    	case COMMAND:
		    		capabilities.addCommand(method);
		    		break;
		    	case QUERY:
		    		capabilities.addQuery(method);
		    		break;
		    	case SUBRESOURCE:
		    		capabilities.addResource(method.name());
		    		break;
		    	}
	    	}
	    }
	    if (resource instanceof IndexResource ) {
	    	capabilities.setIndex(((IndexResource) resource).index());
	    }
	    return restReflection().renderCapabilities(capabilities);
	}

	private Response post(Object resource, String method, MultivaluedMap<String,String> formParams, InputStream stream) {
	    Capability m = ResourceUtil.findMethod(resource, method);
	    if ( !m.isCommand() ) throw new WebApplicationException( HttpServletResponse.SC_NOT_FOUND );
	    try {
		    m.post(resource, request.getContentType(), formParams, stream);
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
	    return Response.ok().status(200).build();
	}
}
