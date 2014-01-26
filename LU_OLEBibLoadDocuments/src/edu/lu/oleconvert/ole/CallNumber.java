package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
@XmlType(name="callNumber", propOrder={"type", "prefix", "number", "classificationPart", "itemPart", "shelvingScheme", "shelvingOrder"})
public class CallNumber implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3652171135563372983L;

	private String type;
	private String prefix;
	private String number;
	private ShelvingScheme shelvingScheme;
	private ShelvingOrder shelvingOrder;
	
	// optional ones, which should be present for the callNumber inside an item, but not the one inside an OLEHoldings
	private String classificationPart;
	private String itemPart;
	
	public CallNumber() {
		super();
		type = prefix = number = "";
		shelvingScheme = new ShelvingScheme();
		shelvingOrder = new ShelvingOrder();
	}
	
	@XmlElement(name="classificationPart")
	public String getClassificationPart() {
		return classificationPart;
	}

	public void setClassificationPart(String classificationPart) {
		this.classificationPart = classificationPart;
	}

	@XmlElement(name="itemPart")
	public String getItemPart() {
		return itemPart;
	}

	public void setItemPart(String itemPart) {
		this.itemPart = itemPart;
	}

	// TODO: I guess we'll need a new class for this one?
	// There's a table OLE_DS_CALL_NUMBER_TYPE_T, which this
	// should be related to
	@XmlElement(name="type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name="CALL_NUMBER_PREFIX")
	@XmlElement(name="prefix", required=true, nillable=true)
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Column(name="CALL_NUMBER")
	@XmlElement(name="number")
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@XmlElement(name="shelvingScheme")	
	public ShelvingScheme getShelvingScheme() {
		return shelvingScheme;
	}

	public void setShelvingSchema(ShelvingScheme shelvingSchema) {
		this.shelvingScheme = shelvingSchema;
	}

	@Embedded
	@XmlElement(name="shelvingOrder")	
	public ShelvingOrder getShelvingOrder() {
		return shelvingOrder;
	}

	public void setShelvingOrder(ShelvingOrder shelvingOrder) {
		this.shelvingOrder = shelvingOrder;
	}	
	
}
