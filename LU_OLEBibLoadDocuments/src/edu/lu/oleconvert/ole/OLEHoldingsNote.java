package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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
public class OLEHoldingsNote implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8646311373822424985L;
	
	Long id;
	OLEHoldings oleHoldings;
	String type;
	String note;
	
	public OLEHoldingsNote() {
		super();
	}

	@Id
	@Column(name="HOLDINGS_NOTE_ID")
	public Long getId() {
		return this.id;
	}
	public void setId(Long i) {
		this.id = i;
	}
	
	@ManyToOne
	@JoinColumn(name="HOLDINGS_ID")
	public OLEHoldings getItem() {
		return this.oleHoldings;
	}
	public void setItem(OLEHoldings oh) {
		this.oleHoldings = oh;
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
