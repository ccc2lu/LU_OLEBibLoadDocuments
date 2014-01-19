package edu.lu.oleconvert.locationingest;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name="locationGroup")
@XmlType(name="locationGroup")
public class LocationGroup implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6091421377621967156L;

	ArrayList<IngestLocation> locations;
	
	public LocationGroup() {
		locations = new ArrayList<IngestLocation>();
	}

	@XmlElement(name="location")
	public ArrayList<IngestLocation> getLocations() {
		return locations;
	}

	public void setLocations(ArrayList<IngestLocation> locations) {
		this.locations = locations;
	}
	
}
