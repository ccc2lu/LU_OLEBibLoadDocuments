package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@Entity
@XmlType(name="note")
public class Note implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8646311373822424985L;
	
	Item item;
	String type;
	String note;
	
	public Note() {
		super();
	}

	@ManyToOne
	@JoinColumn(name="ITEM_ID")
	public Item getItem() {
		return this.item;
	}
	public void setItem(Item i) {
		this.item = i;
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
