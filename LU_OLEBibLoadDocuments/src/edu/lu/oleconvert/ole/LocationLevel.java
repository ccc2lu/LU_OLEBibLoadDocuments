package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name="ole_locn_level_t")
public class LocationLevel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6747313166383932343L;
	
	private String id;
	private String objectId;
	private int versionNbr;
	private String code;
	private String name;
	private LocationLevel parentLevel;
	
	public LocationLevel() {
		versionNbr = 1;
		this.setObjectId(UUID.randomUUID().toString());
	}

	@Id
	@Column(name="LEVEL_ID")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name="OBJ_ID")
	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	@Column(name="VER_NBR")
	public int getVersionNbr() {
		return versionNbr;
	}

	public void setVersionNbr(int versionNbr) {
		this.versionNbr = versionNbr;
	}

	@Column(name="LEVEL_CD")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name="LEVEL_NAME")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne
	@JoinColumn(name="PARENT_LEVEL", referencedColumnName="LEVEL_ID")
	public LocationLevel getParentLevel() {
		return parentLevel;
	}

	public void setParentLevel(LocationLevel parentLevel) {
		this.parentLevel = parentLevel;
	}
	
	
}
