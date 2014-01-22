package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
@XmlType(name="location")
public class Location implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -574288814004619433L;
	private LocationLevel locLevel;
	
	public Location() {
		super();
		locLevel = new LocationLevel();
	}

	@Embedded
	@XmlElement(name="locationLevel")
	public LocationLevel getLocLevel() {
		return locLevel;
	}

	public void setLocLevel(LocationLevel locLevel) {
		this.locLevel = locLevel;
	}

}
