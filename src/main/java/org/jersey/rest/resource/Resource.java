package org.jersey.rest.resource;

import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONUnmarshaller;
import com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl;
import org.jersey.rest.RestfulJerseyService;
import org.jersey.rest.constraint.Constraint;
import org.jersey.rest.constraint.ConstraintEvaluator;
import org.jersey.rest.dto.BaseDTO;
import org.jersey.rest.dto.StringDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all resources.
 *
 * This class has the ability to reflectively generate an HTML
 * representation that lists the resources' capabilities
 *
 */
abstract public class Resource {


    /**
     * lists the capabilities of this resource.
     *
     * @return string containing html
     */
    public String capabilities( ) {
        List<ResourceMethod> methods = new ArrayList<ResourceMethod>();

        StringBuilder sb = new StringBuilder();
        Class<? extends Resource> clazz = this.getClass();
        sb.append( "<h1>" ).append( clazz.getName() ).append("</h1>");
        for ( Method method : clazz.getDeclaredMethods() ) {
            if ( method.isSynthetic() ) continue;
            methods.add(new ResourceMethod(method));
        }
        role( HtmlHelper.class).addResourceMethods(sb, methods);

        sb.append("<h2>Index</h2>");
        if ( this instanceof IndexResource ) {
            sb.append(((IndexResource) this).index());
        }
        return sb.toString();
    }

    public Response post(String method, MultivaluedMap<String,String> formParams, InputStream stream) {
        ResourceMethod m = findMethod( method );
        if ( !m.isCommand() ) throw new WebApplicationException( HttpServletResponse.SC_NOT_FOUND );

        Object[] arguments = stream == null ? arguments(m.method, formParams ) : arguments( m.method, stream );
        return invokeCommand(m.method, this, arguments );
    }


    private Object[] arguments( Method m, InputStream xml ) {
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
                    JAXBContext context = JAXBContextImpl.newInstance(dto.newInstance().getClass());
                    return new Object[] { context.createUnmarshaller().unmarshal( xml ) };
                }
            }
            return new Object[0];
        } catch (Exception e) {
            throw new WebApplicationException( e, Response.Status.BAD_REQUEST );
        }
    }

    private Object[] arguments( Method m, MultivaluedMap<String, String> formParams ) {
        if ( m.getParameterTypes().length == 1 ) {
            Class<?> dto = m.getParameterTypes()[0];
            return new Object[]{ populateDTO( dto, formParams, dto.getSimpleName() ) };
        }
        return new Object[0];
    }

    private boolean checkConstraint(Method method) {
        for ( Annotation a : method.getAnnotations() ) {
            if ( a.annotationType().getAnnotation(Constraint.class) != null ) {
                return constraintEvaluator( a );
            }
        }
        return true;
    }

    private boolean constraintEvaluator( Annotation annotation ) {
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

    public Resource invokePathMethod( String path ) {
        try {
            ResourceMethod method = findMethod( path );
            if ( method.isSubResource() ) {
                try {
                    return (Resource) method.method.invoke(this);
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
            if ( this instanceof IdResource ) {
                return ((IdResource) this).id( new StringDTO( path ) );
            }
        }
        throw new WebApplicationException( Response.Status.NOT_FOUND );
    }


    protected <T> T role(Class<T> clazz) {
        ContextMap map = RestfulJerseyService.getContextMap();
        return map.get(clazz);
    }

    protected <T> void addRole( Class<T> clazz, T instance ) {
        ContextMap map = RestfulJerseyService.getContextMap();
        map.put(clazz, instance);
    }

    public ResourceMethod findMethod( String name ) {
        Class<? extends Resource> clazz = this.getClass();
        for ( Method method : clazz.getDeclaredMethods() ) {
            if ( method.getName().equals( name ) ) {
                ResourceMethod resourceMethod = new ResourceMethod( method );
                if ( !resourceMethod.isIllegal() ) return resourceMethod;
            }
        }
        throw new WebApplicationException( Response.Status.NOT_FOUND );
    }

    public void invokeDelete() {
        if ( this instanceof DeletableResource) {
            (( DeletableResource) this).delete();
        } else {
            throw new WebApplicationException( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
        }
    }

    protected <T extends Resource> Response invokeCommand(Method method, T instance, Object... arguments) {
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

    private Response successResponse() {
        return Response.ok( "Operation Completed Successfully" ).status(200).build();
    }

    public Object get( String get ) {
        ResourceMethod m = findMethod(get);
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
                    result = m.method.invoke(this, populateDTO( dto, queryParams, dto.getSimpleName() ) );
                } else {
                    result = m.method.invoke( this );
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


    protected Object populateDTO(Class<?> dto, MultivaluedMap<String, String> formParams, String prefix ) {
        try {
            Object o = dto.newInstance();
            for ( Field f : o.getClass().getDeclaredFields() ) {
                if ( Modifier.isFinal( f.getModifiers() ) ) continue;
                f.setAccessible(true);
                String value = formParams.getFirst(prefix + "." + f.getName());
                if ( f.getType() == String.class ) {
                    f.set( o, value );
                } else if ( f.getType().isEnum() ) {
                    f.set( o, Enum.valueOf((Class<Enum>) f.getType(), value));
                } else if ( f.getType() == Integer.class ) {
                    f.set( o, Integer.valueOf( value ) );
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


    class ResourceMethod {
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
            if ( parameterTypes.length > 1 ) return false;
            if ( parameterTypes.length == 1 ) {
                return BaseDTO.class.isAssignableFrom( parameterTypes[0] );
            }
            return true;
        }

        private void handleReturnType( Class<?> returnType ) {
            if ( returnType.equals( Void.TYPE ) ) {
                type = MethodType.COMMAND;
            } else if ( Resource.class.isAssignableFrom( returnType ) ) {
                if ( method.getParameterTypes().length == 0 ) type = MethodType.SUBRESOURCE;
                else type = MethodType.ILLEGAL;
            } else if ( BaseDTO.class.isAssignableFrom( returnType )){
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

    private enum MethodType {
        COMMAND, QUERY, SUBRESOURCE, ILLEGAL
    }

}
