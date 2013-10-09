package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="typeOrSource")
public class TypeOrSource implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7451974768144848934L;

	private String pointer;
	private String text;
	
	public TypeOrSource() {
		super();
		// Assigning an empty string here, in conjunction with the required property of the XmlElement annotations below,
		// forces an empty tag to be generated for these elements.
		pointer = "";
		text = "";
	}

	@XmlElement(name="pointer", required=true, nillable=true)
	public String getPointer() {
		return pointer;
	}

	public void setPointer(String pointer) {
		this.pointer = pointer;
	}

	@XmlElement(name="text", required=true, nillable=true)
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	
}
