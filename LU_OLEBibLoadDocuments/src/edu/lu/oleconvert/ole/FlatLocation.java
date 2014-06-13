package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
public class FlatLocation implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1560568398796499235L;
	
	private String level;
	private String locCodeString;
	
	public FlatLocation() {
		super();
		level = "";
		locCodeString = "";
	}

	@Column(name="LOCATION_LEVEL")
	@XmlElement(name="level")
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@Column(name="LOCATION")
	@XmlElement(name="name")
	public String getLocCodeString() {
		return locCodeString;
	}

	public void setLocCodeString(String str) {
		this.locCodeString = str;
	}

}
