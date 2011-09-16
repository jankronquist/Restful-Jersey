package com.jayway.jersey.rest.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.jayway.jersey.rest.RestfulJerseyService;
import com.jayway.jersey.rest.resource.ResourceMethod;

public final class HtmlRestReflection implements RestReflection {
	
	public static final RestReflection INSTANCE = new HtmlRestReflection();
	
	private HtmlRestReflection() {
	}

	@Override
	public Object renderCapabilities(Capabilities capabilities) {
        StringBuilder results = new StringBuilder( );
        results.append("<h1>"+ capabilities.getName()  +"</h1>");
        if (!capabilities.getQueries().isEmpty()) {
            results.append("<h2>Queries</h2>");
            results.append("<ul>");
	    	for (ResourceMethod method: capabilities.getQueries()) {
	    		String path = method.name();
	            results.append("<li><a href='").append(path).append("'>").append(path).append("</a></li>");
	    	}
            results.append("</ul>");
        }
        if (!capabilities.getCommands().isEmpty()) {
            results.append("<h2>Commands</h2>");
            results.append("<ul>");
	    	for (ResourceMethod method: capabilities.getCommands()) {
	    		String path = method.name();
	            results.append("<li><a href='").append(path).append("'>").append(path).append("</a></li>");
	    	}
            results.append("</ul>");
        }
        if (!capabilities.getResources().isEmpty()) {
            results.append("<h2>Sub Resources</h2>");
            results.append("<ul>");
	    	for (String resource: capabilities.getResources()) {
	            results.append("<li><a href='").append(resource).append("'>").append(resource).append("</a></li>");
	    	}
            results.append("</ul>");
        }
		return results.toString();
	}

	@Override
	public Object renderCommandForm(Method method) {
		return createForm(method, "POST");
	}

	@Override
	public Object renderQueryForm(Method method) {
		return createForm(method, "GET");
	}

    /**
     * Generates an HTML form based on the method argument.
     * It will reflectively look at the argument for the method,
     * which has to be a single argument of DTO type, and
     * construct the form based on that
     *
     * @param method
     * @return html form getting the parameters needed for the method
     */
    protected String createForm( Method method, String httpMethod ) {
        Class<?>[] types = method.getParameterTypes();
        StringBuilder sb = new StringBuilder();
        sb.append( "<form name='generatedform' action='").append(method.getName()).
                append("' method='").append(httpMethod).append("' >" );

        for ( Class<?> type : types ) {
            createForm( type.getSimpleName(), type, sb, type.getSimpleName() );
        }
        return sb.append( "<input type='submit' /></form>" ).toString();
    }

    private static void createForm( String legend, Class<?> dto, StringBuilder sb, String fieldPath ) {
        sb.append("<fieldset><legend>").append(legend).append("</legend>");
        for ( Field f : dto.getDeclaredFields() ) {
            if ( Modifier.isFinal(f.getModifiers())) continue;
            String name = f.getName();
            Class<?> type = f.getType();
            // this must be one of the getters
            if (RestfulJerseyService.basicTypes.contains(  type ) ) {
                sb.append(name).append(": <input type='").
                        append( name.equals("password")? "password": "text" ).
                        append("' name='").append( fieldPath ).append( "." ).append( name).append("'/></br>");
            } else if ( type.isEnum() ) {
                sb.append(name).append( ": <select name='").append( fieldPath ).append( "." ).append(name).append("'>");
                for ( Object o : type.getEnumConstants() ) {
                    sb.append( "<option value='").append(o).append("'>").append(o).append("</option>");
                }
                sb.append("</select></br>");
            } else {
                // for now assume DTO subtype
                createForm( name, type, sb, fieldPath + "." + name );
            }
        }
        sb.append("</fieldset>");
    }

}
