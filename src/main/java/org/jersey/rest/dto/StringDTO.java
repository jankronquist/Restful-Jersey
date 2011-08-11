package org.jersey.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "stringholder" )
public class StringDTO implements BaseDTO {

	@XmlElement( name = "string" )
	private String string;

	public StringDTO() {}
	public StringDTO( String string ) {
		this.string = string;
	}

	public String string() {
		return string;
	}

	@Override
	public String toString() {
		return string;
	}
}
