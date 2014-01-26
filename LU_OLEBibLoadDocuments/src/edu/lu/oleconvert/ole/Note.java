package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@Entity
@Table(name="ole_ds_item_note_t")
@XmlType(name="note")
public class Note implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8646311373822424985L;
	
	private Long id;
	private Item item;
	private String type;
	private String note;
	
	public Note() {
		super();
	}

	@Id
	@GeneratedValue
	@Column(name="ITEM_NOTE_ID")
	public Long getId() {
		return this.id;
	}
	public void setId(Long id) {
		this.id = id;
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
