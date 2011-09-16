package com.jayway.jersey.rest.reflection;

import java.lang.reflect.Method;
import java.util.List;

import com.jayway.jersey.rest.resource.ResourceMethod;

public final class JsonRestReflection implements RestReflection {
	
	public static final RestReflection INSTANCE = new JsonRestReflection();
	
	private JsonRestReflection() {
	}

	@Override
	public Object renderCapabilities(Capabilities capabilities) {
        StringBuilder results = new StringBuilder( );
        results.append("{");
        results.append("name: \""+ capabilities.getName()  +"\"");
        if (!capabilities.getQueries().isEmpty()) {
            toList(",queries", capabilities.getQueries(), results);
        }
        if (!capabilities.getCommands().isEmpty()) {
            toList(",commands", capabilities.getCommands(), results);
        }
        if (!capabilities.getResources().isEmpty()) {
    		results.append(",resources");
    		results.append(": [");
    		for (int indx=0; indx<capabilities.getResources().size(); indx++) {
    			String path = capabilities.getResources().get(indx);
    			if (indx > 0) {
    				results.append(",");
    			}
    		    results.append(path);
    		}
    		results.append("]");
        }
        results.append("}");
		return results.toString();
	}

	private void toList(String name, List<ResourceMethod> list, StringBuilder results) {
		results.append(name);
		results.append(": [");
		for (int indx=0; indx<list.size(); indx++) {
			ResourceMethod method = list.get(indx);
			String path = method.name();
			if (indx > 0) {
				results.append(",");
			}
		    results.append("\"" + path + "\"");
		}
		results.append("]");
	}

	@Override
	public Object renderCommandForm(Method method) {
		return createForm(method, "POST");
	}

	@Override
	public Object renderQueryForm(Method method) {
		return createForm(method, "GET");
	}

    protected String createForm( Method method, String httpMethod ) {
    	return "N/A";
    }

}
