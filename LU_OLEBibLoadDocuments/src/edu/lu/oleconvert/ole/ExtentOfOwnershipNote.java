package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@Entity
@XmlType(name="note")
public class ExtentOfOwnershipNote implements Serializable {


	Long id;
	ExtentOfOwnership eoo;
	String type;
	String note;
	
	public ExtentOfOwnershipNote() {
		super();
	}

	@Id
	@Column(name="EXT_NOTE_ID")
	public Long getId() {
		return this.id;
	}
	public void setId(Long i) {
		this.id = i;
	}
	
	@ManyToOne
	@JoinColumn(name="EXT_OWNERSHIP_ID")
	public ExtentOfOwnership getExtentOfOwnership() {
		return this.eoo;
	}
	public void setExtentOfOwnership(ExtentOfOwnership ext) {
		this.eoo = ext;
	}
	
	@XmlAttribute(name="type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlValue
	public String getNote() {
		return note;
	}

	public void setNote(String value) {
		this.note = value;
	}
	
}
