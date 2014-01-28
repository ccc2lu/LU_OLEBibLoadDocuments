package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="extension")
public class Extension implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1774511483976910027L;

	private String displayLabel;
	private List<String> AnyElements;
	
	public Extension() {
		super();
		AnyElements = new ArrayList<String>();
		AnyElements.add(new String(""));
	}

	@XmlAttribute(name="displayLabel")
	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}

	@XmlElement(name="AnyElement", required=true, nillable=true)
	public List<String> getAnyElements() {
		return AnyElements;
	}

	public void setAnyElements(List<String> anyElements) {
		AnyElements = anyElements;
	}
	
	
}
