package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
@XmlType(name="locationLevel")
public class LocationLevel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6747313166383932343L;
	private String level;
	private String name;
	private LocationLevel subLocationLevel;
	
	public LocationLevel() {
		super();
		level = "";
		name = "";
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
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Embedded
	@XmlElement(name="locationLevel")
	public LocationLevel getSubLocationLevel() {
// can't do this or there will be a stack overflow exception
		//		if ( subLocationLevel == null ) {
//			subLocationLevel = new LocationLevel();
//		}
		return subLocationLevel;
	}

	public void setSubLocationLevel(LocationLevel subLocationLevel) {
		this.subLocationLevel = subLocationLevel;
	}

}
