package com.jayway.jersey.rest.reflection;



public interface RestReflection {
	Object renderCapabilities(Capabilities capabilities);
	Object renderCommandForm(Capability capability);
	Object renderQueryForm(Capability capability);
}
