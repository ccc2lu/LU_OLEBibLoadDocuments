package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="formerResourceIdentifier")
public class FormerIdentifier implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5118703088060548533L;
	
	private Identifier identifier;
	private String identifierType;

	public FormerIdentifier() {
		super();
		this.identifier = new Identifier();
		this.identifierType = "";
	}

	@XmlElement(name="identifier")
	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	@XmlElement(name="identifierType")
	public String getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(String identifierType) {
		this.identifierType = identifierType;
	}
	
	
}
