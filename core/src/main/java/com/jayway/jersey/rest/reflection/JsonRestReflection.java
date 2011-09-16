package com.jayway.jersey.rest.reflection;

import java.util.List;


public final class JsonRestReflection implements RestReflection {
	
	public static final RestReflection INSTANCE = new JsonRestReflection();
	
	private JsonRestReflection() {
	}

	@Override
	public Object renderCapabilities(Capabilities capabilities) {
        StringBuilder results = new StringBuilder( );
        results.append("{");
        results.append("\"name\": \""+ capabilities.getName()  +"\"");
        if (!capabilities.getQueries().isEmpty()) {
            toList(", \"queries\"", capabilities.getQueries(), results);
        }
        if (!capabilities.getCommands().isEmpty()) {
            toList(", \"commands\"", capabilities.getCommands(), results);
        }
        if (!capabilities.getResources().isEmpty()) {
    		results.append(", \"resources\"");
    		results.append(": [");
    		for (int indx=0; indx<capabilities.getResources().size(); indx++) {
    			String path = capabilities.getResources().get(indx);
    			if (indx > 0) {
    				results.append(",");
    			}
    		    results.append("\"" + path + "\"");
    		}
    		results.append("]");
        }
        results.append("}");
		return results.toString();
	}

	private void toList(String name, List<Capability> list, StringBuilder results) {
		results.append(name);
		results.append(": [");
		for (int indx=0; indx<list.size(); indx++) {
			Capability method = list.get(indx);
			String path = method.name();
			if (indx > 0) {
				results.append(",");
			}
		    results.append("{");
		    results.append("\"name\":");
		    results.append("\"" + path + "\"");
		    results.append(",");
		    results.append("\"args\":");
		    results.append("[");
		    List<Parameter> parameters = method.getParameters();
		    for (int indy=0; indy<parameters.size(); indy++) {
		    	if (indy > 0) {
					results.append(",");
		    	}
			    results.append("\"" + parameters.get(indy).getName() + "\"");
		    }
		    results.append("]");
		    results.append("}");
		}
		results.append("]");
	}

	@Override
	public Object renderCommandForm(Capability capability) {
		return createForm(capability, "POST");
	}

	@Override
	public Object renderQueryForm(Capability capability) {
		return createForm(capability, "GET");
	}

    protected String createForm(Capability capability, String httpMethod ) {
    	return "N/A";
    }

}
