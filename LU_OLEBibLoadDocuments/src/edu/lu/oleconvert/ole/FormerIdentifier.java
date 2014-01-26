package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name="ole_ds_former_identifier_t")
@XmlType(name="formerResourceIdentifier")
public class FormerIdentifier implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5118703088060548533L;
	
	private Identifier identifier;
	private String identifierType;
	private Item item;
	
	public FormerIdentifier() {
		super();
		this.identifier = new Identifier();
		this.identifierType = "";
	}

	@Column(name="value")
	@XmlElement(name="identifier")
	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	@Column(name="type")
	@XmlElement(name="identifierType")
	public String getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(String identifierType) {
		this.identifierType = identifierType;
	}
	
	@ManyToOne
	@JoinColumn(name="ITEM_ID")
	public Item getItem() {
		return this.item;
	}
	public void setItem(Item it) {
		this.item = it;
	}
	
}
