package org.jersey.rest.resource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Contains helper methods for producing html
 */
public class HtmlHelper {

    public void addResourceMethods( StringBuilder sb, List<Resource.ResourceMethod> methods ) {
    	StringBuilder queries = new StringBuilder( );
    	StringBuilder commands = new StringBuilder( );
        StringBuilder subResources = new StringBuilder( );
    	
    	for ( Resource.ResourceMethod method: methods ) {
    		String path = method.name();
    		if ( method.isQuery() ) {
                queries.append("<li><a href='").append(path).append("'>").append(path).append("</a></li>");
    		} else if ( method.isCommand() ) {
    			commands.append("<li><a href='").append(path).append("'>").append(path).append("</a></li>");
    		} else if ( method.isSubResource() ) {
                subResources.append("<li><a href='").append(path).append( "/'>").append(path).append("</a></li>");
    		}
    	}
    	appendListIfNotEmpty( sb, queries, "<h2>Queries</h2>" );
    	appendListIfNotEmpty( sb, commands, "<h2>Commands</h2>" );
    	appendListIfNotEmpty( sb, subResources, "<h2>Sub Resources</h2>" );
    }

    private void appendListIfNotEmpty( StringBuilder appendTo, StringBuilder list, String title ) {
    	appendTo.append( title );
    	if ( list.length() > 0 ) {
    		appendTo.append( "<ul>" ).append( list ).append( "</ul>" ); 
    	}
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
            if ( type == String.class || type == Integer.class ) {
                sb.append(name).append(": <input type='").
                        append( name.equals("password")? "password": "text" ).
                        append("' name='").append( fieldPath ).append( "." ).append( name).append("'/></br>");
            } else if ( type.isEnum() ) {
                sb.append(name).append( ": <select name='").append( fieldPath ).append( "." ).append(name).append("'>");
                for ( Object o : type.getEnumConstants() ) {
                    sb.append( "<option value='").append(o).append("'>").append(o).append("</option>");
                }
                sb.append( "</select></br>");
            } else {
                // for now assume DTO subtype
                createForm( name, type, sb, fieldPath + "." + name );
            }
        }
        sb.append("</fieldset>");
    }

}
