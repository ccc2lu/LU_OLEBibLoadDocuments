package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="oleHoldings", propOrder={"holdingsIdentifier", "receiptStatus", "uri", "notes", "location", "callNumber", "extentOfOwnership" })
public class OLEHoldings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7571390668933186865L;

	private String primary;
	private String holdingsIdentifier;
	private String receiptStatus;
	private URI uri;
	private ArrayList<Note> notes;
	private Location location;
	private CallNumber callNumber;
	private ArrayList<ExtentOfOwnership> extentOfOwnership;
	
	@XmlElement(name="extentOfOwnership")
	public ArrayList<ExtentOfOwnership> getExtentOfOwnership() {
		return extentOfOwnership;
	}

	public void setExtentOfOwnership(ArrayList<ExtentOfOwnership> extentOfOwnership) {
		this.extentOfOwnership = extentOfOwnership;
	}

	@XmlElement(name="callNumber")
	public CallNumber getCallNumber() {
		return callNumber;
	}

	public void setCallNumber(CallNumber callNumber) {
		this.callNumber = callNumber;
	}

	public OLEHoldings() {
		super();
		uri = new URI();
		extentOfOwnership = new ArrayList<ExtentOfOwnership>();
		notes = new ArrayList<Note>();
	}
	
	@XmlAttribute(name="primary")
	public String getPrimary() {
		return primary;
	}
	public void setPrimary(String primary) {
		this.primary = primary;
	}
	
	@XmlElement(name="holdingsIdentifier")
	public String getHoldingsIdentifier() {
		return holdingsIdentifier;
	}
	public void setHoldingsIdentifier(String holdingsIdentifier) {
		this.holdingsIdentifier = holdingsIdentifier;
	}
	
	@XmlElement(name="receiptStatus")
	public String getReceiptStatus() {
		return receiptStatus;
	}
	public void setReceiptStatus(String recpeiptStatus) {
		this.receiptStatus = recpeiptStatus;
	}

	public URI getUri() {
		return uri;
	}
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	@XmlElement(name="note")
	public ArrayList<Note> getNotes() {
		return notes;
	}
	public void setNotes(ArrayList<Note> notes) {
		this.notes = notes;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
