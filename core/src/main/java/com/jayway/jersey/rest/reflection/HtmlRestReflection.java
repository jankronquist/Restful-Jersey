package com.jayway.jersey.rest.reflection;

import java.util.List;

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
	    	for (Capability method: capabilities.getQueries()) {
	    		String path = method.name();
	            results.append("<li><a href='").append(path).append("'>").append(path).append("</a></li>");
	    	}
            results.append("</ul>");
        }
        if (!capabilities.getCommands().isEmpty()) {
            results.append("<h2>Commands</h2>");
            results.append("<ul>");
	    	for (Capability method: capabilities.getCommands()) {
	    		String path = method.name();
	            results.append("<li><a href='").append(path).append("'>").append(path).append("</a></li>");
	    	}
            results.append("</ul>");
        }
        if (!capabilities.getResources().isEmpty()) {
            results.append("<h2>Sub Resources</h2>");
            results.append("<ul>");
	    	for (String resource: capabilities.getResources()) {
	            results.append("<li><a href='").append(resource).append("/'>").append(resource).append("</a></li>");
	    	}
            results.append("</ul>");
        }
		return results.toString();
	}

	@Override
	public Object renderCommandForm(Capability capability) {
		return createForm(capability, "POST");
	}

	@Override
	public Object renderQueryForm(Capability capability) {
		return createForm(capability, "GET");
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
    protected String createForm( Capability capability, String httpMethod ) {
        List<Parameter> parameters = capability.getParameters();
        StringBuilder sb = new StringBuilder();
        sb.append( "<form name='generatedform' action='").append(capability.name()).
                append("' method='").append(httpMethod).append("' >" );

        for ( Parameter parameter : parameters ) {
            createForm( parameter, sb, parameter.getPath());
        }
        return sb.append( "<input type='submit' /></form>" ).toString();
    }

    private static void createForm( Parameter parameter, StringBuilder sb, String fieldPath ) {
        if (parameter.isTextField()) {
            sb.append(parameter.getName()).append(": <input type='").
                    append( parameter.isPassword() ? "password": "text" ).
                    append("' name='").append( fieldPath ).append("'/></br>");
        } else if ( parameter.hasOptions() ) {
            sb.append(parameter.getName()).append( ": <select name='").append( fieldPath ).append("'>");
            for ( Object o : parameter.getOptions() ) {
                sb.append( "<option value='").append(o).append("'>").append(o).append("</option>");
            }
            sb.append("</select></br>");
        } else {
            sb.append("<fieldset><legend>").append(parameter.getName()).append("</legend>");
            for ( Parameter child : parameter.getChildParameters() ) {
            	createForm(child, sb, fieldPath + "." + child.getPath());
            }
            sb.append("</fieldset>");
        }
    }

}
