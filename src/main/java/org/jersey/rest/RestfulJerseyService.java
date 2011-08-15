package org.jersey.rest;

import org.jersey.rest.resource.ContextMap;
import org.jersey.rest.resource.HtmlHelper;
import org.jersey.rest.resource.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.util.*;

/**
 * Extend this class to define your rest
 * service
 */
public abstract class RestfulJerseyService {

    protected abstract Resource root();
    protected abstract void setupContext();

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


    public static Set<Class> basicTypes;

    static {
        basicTypes = new HashSet<Class>();
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
        contextMap.put( HtmlHelper.class, new HtmlHelper() );
        setContextMap(contextMap);

        // call application specific context setup
        setupContext();
    }

    @GET
    @Produces( "text/html;charset=utf-8" )
    @Consumes( "text/html;charset=utf-8" )
    public Object capabilities() {
        setup();
        if ( uriInfo.getPath().endsWith( "/") )
            return root().capabilities();
        else throw new WebApplicationException( Response.Status.NOT_FOUND );
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
            return evaluatePath( pathAndMethod.pathSegments() ).capabilities();
        }
        return evaluatePath( pathAndMethod.pathSegments() ).get(pathAndMethod.method());
    }

    protected Response evaluatePostPut( String path, InputStream stream, MultivaluedMap<String, String> formParams ) {
        PathAndMethod pathAndMethod = new PathAndMethod( path );
        if ( pathAndMethod.method() == null ) {
            throw new WebApplicationException( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
        }
        return evaluatePath( pathAndMethod.pathSegments() ).post(pathAndMethod.method(), formParams, stream);
    }

    protected void evaluateDelete( String path ) {
        PathAndMethod pathAndMethod = new PathAndMethod(path);
        if ( pathAndMethod.method() != null ) {
            throw new WebApplicationException( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
        }
        evaluatePath( pathAndMethod.pathSegments() ).invokeDelete();
    }

    private Resource evaluatePath( List<String> segments ) {
        Resource current = root();
        for ( String pathSegment: segments ) {
            current = current.invokePathMethod(pathSegment);
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
}
