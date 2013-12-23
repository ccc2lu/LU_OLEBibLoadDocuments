package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="temporaryItemType")
public class TemporaryItemType extends ItemType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8411477653644636082L;

	// Since I commented codeValue out in this class's parent class, I have to declare it here
	protected String codeValue;
	
	public TemporaryItemType() {
		super();
		this.codeValue = "";
	}
	
	// codeValue appears to be required for temporaryItemType, even if it's empty
	// so we make it required and assign the empty string to it
	@XmlElement(name="codeValue", required=true, nillable=true)
	public String getCodeValue() {
		return codeValue;
	}

}
