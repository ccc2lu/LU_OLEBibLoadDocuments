package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;

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
@Table(name="ole_ds_item_t")
@XmlType(name="item", propOrder={"analytic", "resourceIdentifier", "itemIdentifier", "purchaseOrderLineItemIdentifier",
		                         "vendorLineItemIdentifier", "accessInformation", "barcodeARSL", "formerIdentifiers",
		                         "statisticalSearchingCodes", "itemType", "location", "copyNumber", "copyNumberLabel",
		                         "volumeNumber", "volumeNumberLabel", "notes", "enumeration", "chronology",
		                         "highDensityStorage", "temporaryItemType", "fund", "donorPublicDisplay", "donorNote",
		                         "callNumber", "price", "numberOfPieces", "itemStatus", "itemStatusEffectiveDate",
		                         "checkinNote", "staffOnlyFlag", "fastAddFlag", "extension"})
public class Item implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3405820085871602535L;
	
	private Instance instance;
	private String analytic;
	private String resourceIdentifier;
	private Long itemIdentifier;
	
	private String purchaseOrderLineItemIdentifier;
	private String vendorLineItemIdentifier;
	private AccessInformation accessInformation; 
	private String barcodeARSL;
	private ArrayList<FormerIdentifier> formerIdentifiers;
	private ArrayList<StatisticalSearchingCode> statisticalSearchingCodes; 
	private ItemType itemType;
	
	Location location;
	private String copyNumber;
	private String copyNumberLabel;
	private String volumeNumber;
	private String volumeNumberLabel;
	private ArrayList<ItemNote> notes;
	private String enumeration;
	private String chronology;
	private HighDensityStorage highDensityStorage;
	//private TemporaryItemType temporaryItemType;
	private ItemType temporaryItemType;
	private String fund;
	private ItemDonor itemDonor;
	//private String donorPublicDisplay;
	//private String donorNote;
	private CallNumber callNumber;
	private CallNumberType callNumberType;
	private String price;
	private String numberOfPieces;
	//private String itemStatus;
	private ItemStatus itemStatus;
	private String itemStatusEffectiveDate;
	private String checkinNote;
	private String staffOnlyFlag;
	private String fastAddFlag;
	private Extension extension;
	
	// This default, no-args constructor only puts a value in those fields
	// that need it initialized so they show up properly in the XML output
	public Item() {
		super();
		this.barcodeARSL = "";
		this.vendorLineItemIdentifier = "";
		this.purchaseOrderLineItemIdentifier = "";
		this.statisticalSearchingCodes = new ArrayList<StatisticalSearchingCode>();
		this.formerIdentifiers = new ArrayList<FormerIdentifier>();
		this.itemType = new ItemType();
		this.callNumber = new CallNumber();
		this.callNumber.setClassificationPart("");
		this.callNumber.setItemPart("");
		this.callNumberType = new CallNumberType();
		this.location = new Location();
		this.highDensityStorage = new HighDensityStorage();
		this.temporaryItemType = new TemporaryItemType();
		//this.extension = new Extension();
		this.notes = new ArrayList<ItemNote>();
		copyNumber = copyNumberLabel = volumeNumber = volumeNumberLabel = "";
		//fund = donorPublicDisplay = donorNote = "";
		fund = "";
		this.itemDonor = new ItemDonor();
		price = numberOfPieces;
		//itemStatus = itemStatusEffectiveDate = "";
		this.itemStatus = new ItemStatus();
		checkinNote = staffOnlyFlag = fastAddFlag = "";
		this.instance = new Instance();
	}
	
	// This copy constructor needs to assign all the fields
	public Item(Item i) {
		super();
		//this.analytic = i.getAnalytic();
		this.resourceIdentifier = i.getResourceIdentifier();
		this.itemIdentifier = i.getItemIdentifier();
		this.barcodeARSL = i.getBarcodeARSL();
		this.vendorLineItemIdentifier = i.getVendorLineItemIdentifier();
		this.purchaseOrderLineItemIdentifier = i.getPurchaseOrderLineItemIdentifier();
		this.statisticalSearchingCodes = (ArrayList<StatisticalSearchingCode>) i.getStatisticalSearchingCodes().clone();
		this.formerIdentifiers = (ArrayList<FormerIdentifier>) i.getFormerIdentifiers().clone();
		this.itemType = i.getItemType();
		this.callNumber = i.getCallNumber();
		this.callNumber.setClassificationPart(i.getCallNumber().getClassificationPart());
		this.callNumber.setItemPart(i.getCallNumber().getClassificationPart());
		this.callNumberType = i.getCallNumberType();
		this.location = i.getLocation();
		this.highDensityStorage = i.getHighDensityStorage();
		this.temporaryItemType = i.getTemporaryItemType();
		//this.extension = i.getExtension();
		this.notes = (ArrayList<ItemNote>) i.getNotes().clone();
		this.enumeration = i.getEnumeration();
		this.chronology = i.getChronology();
		copyNumber = i.getCopyNumber(); 
		copyNumberLabel = i.getCopyNumberLabel(); 
		volumeNumber = i.getVolumeNumber();
		volumeNumberLabel = i.getVolumeNumberLabel();
		fund = i.getFund();
		//donorPublicDisplay = i.getDonorPublicDisplay();
		//donorNote = i.getDonorNote();
		itemDonor = i.getItemDonor();
		price = i.getPrice();
		numberOfPieces = i.getNumberOfPieces();
		itemStatus = i.getItemStatus();
		itemStatusEffectiveDate = i.getItemStatusEffectiveDate();
		checkinNote = i.getCheckinNote();
		staffOnlyFlag = i.getStaffOnlyFlag();
		fastAddFlag = i.getFastAddFlag();
		this.accessInformation = i.getAccessInformation();
		itemStatus = i.getItemStatus();
		this.instance = i.getInstance();
	}
	
	@ManyToOne
	@JoinColumn(name="INSTANCE_ID")
	public Instance getInstance() {
		return this.instance;
	}
	public void setInstance(Instance inst) { 
		this.instance = inst;
	}
	
	@Column(name="COPY_NUMBER")
	@XmlElement(name="copyNumber", required=true, nillable=true)
	public String getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(String copyNumber) {
		this.copyNumber = copyNumber;
	}

	// These next few seem to be gone from the data model now
	@XmlElement(name="copyNumberLabel", required=true, nillable=true)
	public String getCopyNumberLabel() {
		return copyNumberLabel;
	}
	
	public void setCopyNumberLabel(String copyNumberLabel) {
		this.copyNumberLabel = copyNumberLabel;
	}
	
	
	@XmlElement(name="volumeNumber", required=true, nillable=true)
	public String getVolumeNumber() {
		return volumeNumber;
	}

	public void setVolumeNumber(String volumeNumber) {
		this.volumeNumber = volumeNumber;
	}
	
	@XmlElement(name="volumeNumberLabel", required=true, nillable=true)
	public String getVolumeNumberLabel() {
		return volumeNumberLabel;
	}

	public void setVolumeNumberLabel(String volumeNumberLabel) {
		this.volumeNumberLabel = volumeNumberLabel;
	}
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="item")
	@XmlElement(name="note")
	public ArrayList<ItemNote> getNotes() {
		return notes;
	}

	public void setNotes(ArrayList<ItemNote> notes) {
		this.notes = notes;
	}

	@Column(name="enumeration")
	@XmlElement(name="enumeration")
	public String getEnumeration() {
		return enumeration;
	}

	public void setEnumeration(String enumeration) {
		this.enumeration = enumeration;
	}

	@Column(name="chronology")
	@XmlElement(name="chronology")
	public String getChronology() {
		return chronology;
	}

	public void setChronology(String chronology) {
		this.chronology = chronology;
	}

	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="HIGH_DENSITY_STORAGE_ID")
	@XmlElement(name="highDensityStorage")
	public HighDensityStorage getHighDensityStorage() {
		return highDensityStorage;
	}

	public void setHighDensityStorage(HighDensityStorage highDensityStorage) {
		this.highDensityStorage = highDensityStorage;
	}

	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="TEMP_ITEM_TYPE_ID", referencedColumnName="ITEM_TYPE_ID")
	@XmlElement(name="temporaryItemType")
//	public TemporaryItemType getTemporaryItemType() {
	public ItemType getTemporaryItemType() {
		return temporaryItemType;
	}

	//public void setTemporaryItemType(TemporaryItemType temporaryItemType) {
	public void setTemporaryItemType(ItemType temporaryItemType) {
		this.temporaryItemType = temporaryItemType;
	}

	@Column(name="FUND")
	@XmlElement(name="fund", required=true, nillable=true)
	public String getFund() {
		return fund;
	}

	public void setFund(String fund) {
		this.fund = fund;
	}

	@OneToOne(mappedBy="item")
	public ItemDonor getItemDonor() {
		return this.itemDonor;
	}
	public void setItemDonor(ItemDonor ID) {
		this.itemDonor = ID;
	}
	// The below stuff was moved into the separate ole_ds_item_donor_t table, 
	// so I made an ItemDonor class
	/*
	@XmlElement(name="donorPublicDisplay", required=true, nillable=true)
	public String getDonorPublicDisplay() {
		return donorPublicDisplay;
	}

	public void setDonorPublicDisplay(String donorPublicDisplay) {
		this.donorPublicDisplay = donorPublicDisplay;
	}

	@XmlElement(name="donorNote", required=true, nillable=true)
	public String getDonorNote() {
		return donorNote;
	}

	public void setDonorNote(String donorNote) {
		this.donorNote = donorNote;
	}
	*/
	
	@Column(name="PRICE")
	@XmlElement(name="price", required=true, nillable=true)
	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	@Column(name="NUM_PIECES")
	@XmlElement(name="numberOfPieces", required=true, nillable=true)
	public String getNumberOfPieces() {
		return numberOfPieces;
	}

	public void setNumberOfPieces(String numberOfPieces) {
		this.numberOfPieces = numberOfPieces;
	}

	/*
	@XmlElement(name="itemStatus", required=true, nillable=true)
	public String getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}
	*/
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="ITEM_STATUS_ID")
	@XmlElement(name="itemStatus", required=true, nillable=true)
	public ItemStatus getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(ItemStatus itemStatus) {
		this.itemStatus = itemStatus;
	}

	
	// I'm assuming the database's "EFFECTIVE_DATE" field refers to the item status ...
	@Column(name="EFFECTIVE_DATE")
	@XmlElement(name="itemStatusEffectiveDate", required=true, nillable=true)
	public String getItemStatusEffectiveDate() {
		return itemStatusEffectiveDate;
	}

	public void setItemStatusEffectiveDate(String itemStatusEffectiveDate) {
		this.itemStatusEffectiveDate = itemStatusEffectiveDate;
	}

	@Column(name="CHECK_IN_NOTE")
	@XmlElement(name="checkinNote", required=true, nillable=true)
	public String getCheckinNote() {
		return checkinNote;
	}

	public void setCheckinNote(String checkinNote) {
		this.checkinNote = checkinNote;
	}

	@Column(name="STAFF_ONLY")
	@XmlElement(name="staffOnlyFlag", required=true, nillable=true)
	public String getStaffOnlyFlag() {
		return staffOnlyFlag;
	}

	public void setStaffOnlyFlag(String staffOnlyFlag) {
		this.staffOnlyFlag = staffOnlyFlag;
	}

	@Column(name="FAST_ADD")
	@XmlElement(name="fastAddFlag", required=true, nillable=true)
	public String getFastAddFlag() {
		return fastAddFlag;
	}

	public void setFastAddFlag(String fastAddFlag) {
		this.fastAddFlag = fastAddFlag;
	}

	/*
	@XmlElement(name="extension")
	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}
	*/
	
	@Embedded
	@XmlElement(name="location")
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	@Embedded
	@XmlElement(name="callNumber", required=true, nillable=true)
	public CallNumber getCallNumber() {
		return callNumber;
	}

	public void setCallNumber(CallNumber callNumber) {
		this.callNumber = callNumber;
	}

	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="CALL_NUMBER_TYPE_ID")
	public CallNumberType getCallNumberType() {
		return callNumberType;
	}

	public void setCallNumberType(CallNumberType callNumberType) {
		this.callNumberType = callNumberType;
	}
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="ITEM_TYPE_ID")
	@XmlElement(name="itemType")
	public ItemType getItemType() {
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}

	@Column(name="PURCHASE_ORDER_LINE_ITEM_ID")
	@XmlElement(name="purchaseOrderLineItemIdentifier", required=true, nillable=true)
	public String getPurchaseOrderLineItemIdentifier() {
		return purchaseOrderLineItemIdentifier;
	}

	public void setPurchaseOrderLineItemIdentifier(
			String purchaseOrderLineItemIdentifier) {
		this.purchaseOrderLineItemIdentifier = purchaseOrderLineItemIdentifier;
	}

	@Column(name="VENDOR_LINE_ITEM_ID")
	@XmlElement(name="vendorLineItemIdentifier", required=true, nillable=true)
	public String getVendorLineItemIdentifier() {
		return vendorLineItemIdentifier;
	}

	public void setVendorLineItemIdentifier(String vendorLineItemIdentifier) {
		this.vendorLineItemIdentifier = vendorLineItemIdentifier;
	}

	@Embedded
	@XmlElement(name="accessInformation")
	public AccessInformation getAccessInformation() {
		return accessInformation;
	}

	public void setAccessInformation(AccessInformation accessInformation) {
		this.accessInformation = accessInformation;
	}

	@Column(name="BARCODE_ARSL")
	@XmlElement(name="barcodeARSL", required=true, nillable=true)
	public String getBarcodeARSL() {
		return barcodeARSL;
	}

	public void setBarcodeARSL(String barcodeARSL) {
		this.barcodeARSL = barcodeARSL;
	}

	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="item")
	@XmlElement(name="formerIdentifier")
	public ArrayList<FormerIdentifier> getFormerIdentifiers() {
		return formerIdentifiers;
	}

	public void setFormerIdentifiers(ArrayList<FormerIdentifier> formerIdentifiers) {
		this.formerIdentifiers = formerIdentifiers;
	}

	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="STATISTICAL_SEARCHING_ID")
	@XmlElement(name="statisticalSearchingCode")
	public ArrayList<StatisticalSearchingCode> getStatisticalSearchingCodes() {
		return statisticalSearchingCodes;
	}

	public void setStatisticalSearchingCodes(
			ArrayList<StatisticalSearchingCode> statisticalSearchingCodes) {
		this.statisticalSearchingCodes = statisticalSearchingCodes;
	}

	// seems to be gone from the data model now
	/*
	@XmlAttribute(name="analytic")
	public String getAnalytic() {
		return analytic;
	}

	public void setAnalytic(String analytic) {
		this.analytic = analytic;
	}
	*/
	
	@XmlAttribute(name="resourceIdentifier")
	public String getResourceIdentifier() {
		return resourceIdentifier;
	}

	public void setResourceIdentifier(String resourceIdentifier) {
		this.resourceIdentifier = resourceIdentifier;
	}

	@Id
	@GeneratedValue
	@Column(name="ITEM_ID")
	@XmlElement(name="itemIdentifier")
	public Long getItemIdentifier() {
		return itemIdentifier;
	}

	public void setItemIdentifier(Long itemIdentifier) {
		this.itemIdentifier = itemIdentifier;
	}

}
