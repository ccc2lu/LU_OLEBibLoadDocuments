package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="shelvingScheme")
public class ShelvingScheme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4433818065035171488L;

	private String codeValue;
	private String fullValue;
	private TypeOrSource typeOrSource;
	
	public ShelvingScheme() {
		super();
		codeValue = fullValue = "";
		typeOrSource = new TypeOrSource();
	}

	@XmlElement(name="codeValue", required=true, nillable=true)
	public String getCodeValue() {
		return codeValue;
	}
	
	public void setCodeValue(String codeValue) {
		this.codeValue = codeValue;
	}

	@XmlElement(name="fullValue", required=true, nillable=true)
	public String getFullValue() {
		return fullValue;
	}

	public void setFullValue(String fullValue) {
		this.fullValue = fullValue;
	}

	@XmlElement(name="typeOrSource")
	public TypeOrSource getTypeOrSource() {
		return typeOrSource;
	}

	public void setTypeOrSource(TypeOrSource typeOrSource) {
		this.typeOrSource = typeOrSource;
	}
	
	
}
