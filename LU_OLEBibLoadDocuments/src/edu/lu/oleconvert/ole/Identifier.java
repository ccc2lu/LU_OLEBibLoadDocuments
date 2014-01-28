package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
@XmlType(name="identifier")
public class Identifier implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 870296645980784663L;

	//private String source;
	private String identifierValue;
	
	public Identifier() {
		super();
		identifierValue = "";
	}

	/* Gone from the data model
	@XmlAttribute(name="source")
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	*/
	
	@Column(name="VALUE")
	@XmlElement(name="identifierValue", required=true, nillable=true)
	public String getIdentifierValue() {
		return identifierValue;
	}

	public void setIdentifierValue(String identifierValue) {
		this.identifierValue = identifierValue;
	}
	
	
}
