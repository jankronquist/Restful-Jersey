package com.jayway.jersey.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "booleanholder" )
public class BooleanDTO {

	@XmlElement( name = "bool" )
	private boolean bool;

	public BooleanDTO() {}
	public BooleanDTO( boolean bool) {
		this.bool = bool;
	}

	public boolean bool() {
		return bool;
	}

	@Override
	public String toString() {
		return "" + bool;
	}
}
