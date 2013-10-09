package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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

	@XmlElement(name="locationLevel")
	public LocationLevel getLocLevel() {
		return locLevel;
	}

	public void setLocLevel(LocationLevel locLevel) {
		this.locLevel = locLevel;
	}

}
