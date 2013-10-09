package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name="uri")
public class URI implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7453625492271168000L;
	
	private String resolvable;
	private String uri;
	
	public URI() {
		super();
	}
	
	@XmlAttribute(name="resolvable")
	public String getResolvable() {
		return resolvable;
	}
	
	public void setResolvable(String res) {
		this.resolvable = res;
	}

	@XmlValue
	public String getUri() {
		return uri;
	}

	public void setUri(String value) {
		this.uri = value;
	}
	
	
}
