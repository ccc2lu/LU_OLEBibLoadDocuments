package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="itemType", propOrder={"codeValue", "fullValue", "typeOrSource"})
public class ItemType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1290250942532107793L;

	protected String codeValue;
	private String fullValue;
	private TypeOrSource typeOrSource;
	
	public ItemType() {
		super();
		fullValue = "";
		typeOrSource = new TypeOrSource();
	}

	@XmlElement(name="codeValue", required=false)
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

	public void setTypeOrSource(TypeOrSource tos) {
		this.typeOrSource = tos;
	}
	
}
