package edu.lu.oleconvert.locationingest;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="location", propOrder={"locationCode", "locationName", "locationLevelCode", "parentLocationCode"})
public class IngestLocation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8135842955444796103L;
	
	private String locationCode;
	private String locationName;
	private String locationLevelCode;
	private String parentLocationCode;
	
	public IngestLocation() {
		
	}
	
	public IngestLocation(String code, String name, String level, String parent) {
		locationCode = code;
		locationName = name;
		locationLevelCode = level;
		parentLocationCode = parent;
	}
	
	public String getLocationCode() {
		return locationCode;
	}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public String getLocationLevelCode() {
		return locationLevelCode;
	}
	public void setLocationLevelCode(String locationLevelCode) {
		this.locationLevelCode = locationLevelCode;
	}
	public String getParentLocationCode() {
		return parentLocationCode;
	}
	public void setParentLocationCode(String parentLocationCode) {
		this.parentLocationCode = parentLocationCode;
	}
	
	
}
