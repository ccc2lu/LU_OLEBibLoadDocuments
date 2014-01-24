package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name="ole_ds_holdings_t")
@XmlType(name="oleHoldings", propOrder={"holdingsIdentifier", "receiptStatus", "uri", "notes", "location", "callNumber", "extentOfOwnership" })
public class OLEHoldings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7571390668933186865L;

	private String primary;
	private String holdingsIdentifier;
	private ReceiptStatus receiptStatus;
	//private ArrayList<URI> uri;
	private List<AccessURI> accessURIs;
	private List<OLEHoldingsNote> notes;
	private Location location;
	private CallNumber callNumber;
	private CallNumberType callNumberType;
	private List<ExtentOfOwnership> extentOfOwnership;
	private Instance instance;
	
	public OLEHoldings() {
		super();
		accessURIs = new ArrayList<AccessURI>();
		extentOfOwnership = new ArrayList<ExtentOfOwnership>();
		extentOfOwnership = new ArrayList<ExtentOfOwnership>();
		notes = new ArrayList<OLEHoldingsNote>();
		callNumberType = new CallNumberType();
		instance = null;
	}
	
	public OLEHoldings(Instance i) {
		this();
		this.setInstance(i);
	}
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="CALL_NUMBER_TYPE_ID")
	public CallNumberType getCallNumberType() {
		return callNumberType;
	}

	public void setCallNumberType(CallNumberType callNumberType) {
		this.callNumberType = callNumberType;
	}
	
	@OneToOne
	@JoinColumn(name="INSTANCE_ID")
	public Instance getInstance() {
		return this.instance;
	}
	public void setInstance(Instance i ) {
		this.instance = i;
	}
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="oleHoldings", cascade=CascadeType.ALL)
	@XmlElement(name="extentOfOwnership")
	public List<ExtentOfOwnership> getExtentOfOwnership() {
		return extentOfOwnership;
	}

	public void setExtentOfOwnership(ArrayList<ExtentOfOwnership> extentOfOwnership) {
		this.extentOfOwnership = extentOfOwnership;
	}

	@Embedded
	@XmlElement(name="callNumber")
	public CallNumber getCallNumber() {
		return callNumber;
	}

	public void setCallNumber(CallNumber callNumber) {
		this.callNumber = callNumber;
	}
	
	@XmlAttribute(name="primary")
	public String getPrimary() {
		return primary;
	}
	public void setPrimary(String primary) {
		this.primary = primary;
	}
	
	@Id
	@GeneratedValue
	@Column(name="HOLDINGS_ID")
	@XmlElement(name="holdingsIdentifier")
	public String getHoldingsIdentifier() {
		return holdingsIdentifier;
	}
	public void setHoldingsIdentifier(String holdingsIdentifier) {
		this.holdingsIdentifier = holdingsIdentifier;
	}
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="RECEIPT_STATUS_ID")
	@XmlElement(name="receiptStatus")
	public ReceiptStatus getReceiptStatus() {
		return receiptStatus;
	}
	public void setReceiptStatus(ReceiptStatus recpeiptStatus) {
		this.receiptStatus = recpeiptStatus;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="oleholdings", cascade=CascadeType.ALL)
	@XmlElement(name="uri")
	public List<AccessURI> getAccessURIs() {
		return accessURIs;
	}
	public void setAccessURIs(List<AccessURI> uris) {
		this.accessURIs = uris;
	}
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="oleHoldings", cascade=CascadeType.ALL)
	@XmlElement(name="note")
	public List<OLEHoldingsNote> getNotes() {
		return notes;
	}
	public void setNotes(ArrayList<OLEHoldingsNote> notes) {
		this.notes = notes;
	}

	@Embedded
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
