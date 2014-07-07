package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name="ole_locn_t")
@XmlType(name="location")
public class Location implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -574288814004619433L;
	private Long id;
	private String objectId;
	private LocationLevel level;
	private String code;
	private String name;
	private Location parentLocation;
	//private LocationLevel subLocationLevel;
	
	public Location() {
		super();
		code = "";
		name = "";
	}

	/*
	@Column(name="LOCATION_LEVEL")
	@XmlElement(name="level")
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	*/
	
	@Id
	@GeneratedValue
	@Column(name="LOCN_ID")
	public Long getId() {
		return this.id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne
	@JoinColumn(name="LEVEL_ID")
	public LocationLevel getLevel() {
		return level;
	}
	public void setLevel(LocationLevel lvl) {
		this.level = lvl;
	}
	
	@Column(name="LOCN_NAME")
	@XmlElement(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name="LOCN_CD")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	// Not sure if this will work or not, it's self referential ...
	// If not, try making it bi-directional by adding a collection of sublocations
	// with a mappedBy field of parentLocation
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="PARENT_LOCN_ID", referencedColumnName="LOCN_ID")
	public Location getParentLocation() {
		return this.parentLocation;
	}
	public void setParentLocation(Location loc) {
		this.parentLocation = loc;
	}
	
}
