package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlElement;

@Embeddable
public class ShelvingOrder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5200406620518354388L;

	private String shelvingOrder;
	
	public ShelvingOrder() {
		super(); // everything comes from the super class for this one
	}
	
	@Column(name="SHELVING_ORDER")
	public String getShelvingOrder() {
		return shelvingOrder;
	}
	public void setShelvingOrder(String order) {
		this.shelvingOrder = order;
	}
}
