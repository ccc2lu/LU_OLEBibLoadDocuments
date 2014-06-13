package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.lu.oleconvert.LU_DBLoadInstances;

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
	
	private OLEHoldings itemHoldings;
	//private String analytic;
	//private String resourceIdentifier;
	private Long itemIdentifier;
	
	private String purchaseOrderLineItemIdentifier;
	private String vendorLineItemIdentifier;
	private AccessInformation accessInformation; 
	private String barcodeARSL;
	private List<FormerIdentifier> formerIdentifiers;
	private List<ItemStatSearch> statSearches; 
	// ole_ds_item_type_t removed from the database, apparently
	//private ItemType itemType;
	private Deliver_ItemType itemType;
	
	FlatLocation location;
	String locationLevel;
	
	private String copyNumber;
	// Not in the data model anymore in 1.5, apparently
	//private String copyNumberLabel;
	//private String volumeNumber;
	//private String volumeNumberLabel;
	private List<ItemNote> notes;
	private String enumeration;
	private String chronology;
	private HighDensityStorage highDensityStorage;
	//private TemporaryItemType temporaryItemType;
	private Deliver_ItemType temporaryItemType;
	private String fund;
	private ItemDonor itemDonor;
	//private String donorPublicDisplay;
	//private String donorNote;
	private CallNumber callNumber;
	private CallNumberType callNumberType;
	
	private double price;
	private String numberOfPieces;
	//private String itemStatus;
	// ole_ds_item_status_t is gone, only using deliver version now, apparently
	//private ItemStatus itemStatus;
	private Deliver_ItemStatus itemStatus;
	private String itemStatusDateUpdated;
	private String checkinNote;
	private String staffOnlyFlag;
	private String fastAddFlag;
	//private Extension extension;
	
	private String claimsReturnedFlag;
	private String claimsReturnedFlagCreateDate;
	private String claimsReturnedNote;
	
	private String currentBorrower;
	private String proxyBorrower;
	private String dueDateTime;
	private String itemDmgStatus;
	private String itemDmgNote;
	private String missingPieces;
	private String missingPiecesNote;
	private String missingPiecesEffectDate;
	private Long missingPiecesCount;
	
	private String uniqueIdPrefix;
	private String createdDate;
	private String updatedDate;
	private String createdBy;
	private String updatedBy;
	
	// This default, no-args constructor only puts a value in those fields
	// that need it initialized so they show up properly in the XML output
	public Item() {
		super();
		this.barcodeARSL = "";
		this.vendorLineItemIdentifier = "";
		this.purchaseOrderLineItemIdentifier = "";
		this.statSearches = new ArrayList<ItemStatSearch>();
		this.formerIdentifiers = new ArrayList<FormerIdentifier>();
		// We don't want to make new instances of these objects or they'll
		// be persisted with just a bunch of null values
		//this.itemType = new ItemType();
		//this.callNumber = new CallNumber();
		//this.callNumber.setClassificationPart("");
		//this.callNumber.setItemPart("");
		//this.callNumberType = new CallNumberType();
		//this.location = new FlatLocation();
		//this.highDensityStorage = new HighDensityStorage();
		//this.temporaryItemType = new TemporaryItemType();
		//this.extension = new Extension();
		this.notes = new ArrayList<ItemNote>();
		copyNumber = "";
		//copyNumberLabel = volumeNumber = volumeNumberLabel = "";
		//fund = donorPublicDisplay = donorNote = "";
		fund = "";
		//this.itemDonor = new ItemDonor();
		price = 0;
		numberOfPieces = "";
		//itemStatus = itemStatusEffectiveDate = "";
		itemStatusDateUpdated = "";
		//this.itemStatus = new ItemStatus();
		checkinNote = staffOnlyFlag = fastAddFlag = "";
		//this.itemHoldings = new OLEHoldings();
		//itemStatus = new ItemStatus();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String datestr = df.format(Calendar.getInstance().getTime());
		this.setCreatedDate(datestr);
		this.setCreatedBy("BulkIngest-User");
		this.setUpdatedDate(datestr);
		this.setUpdatedBy("BulkIngest-User");
		this.setItemStatusDateUpdated(datestr);
		this.setUniqueIdPrefix("wio");
	}
	
	// This copy constructor needs to assign all the fields
	public Item(Item i) {
		super();
		//this.analytic = i.getAnalytic();
		//this.resourceIdentifier = i.getResourceIdentifier();
		this.itemIdentifier = i.getItemIdentifier();
		this.barcodeARSL = i.getBarcodeARSL();
		this.vendorLineItemIdentifier = i.getVendorLineItemIdentifier();
		this.purchaseOrderLineItemIdentifier = i.getPurchaseOrderLineItemIdentifier();
		//this.statisticalSearchingCodes = (List<StatisticalSearchingCode>) i.getStatisticalSearchingCodes().clone();
		//this.formerIdentifiers = (List<FormerIdentifier>) i.getFormerIdentifiers().clone();
		this.statSearches = i.getStatSearches();
		this.formerIdentifiers = i.getFormerIdentifiers();
		this.itemType = i.getItemType();
		this.callNumber = i.getCallNumber();
		//this.callNumber.setClassificationPart(i.getCallNumber().getClassificationPart());
		//this.callNumber.setItemPart(i.getCallNumber().getClassificationPart());
		this.callNumberType = i.getCallNumberType();
		this.location = i.getLocation();
		this.locationLevel = i.getLocationLevel();
		this.highDensityStorage = i.getHighDensityStorage();
		this.temporaryItemType = i.getTemporaryItemType();
		//this.extension = i.getExtension();
		//this.notes = (ArrayList<ItemNote>) i.getNotes().clone();
		this.notes = i.getNotes();
		this.enumeration = i.getEnumeration();
		this.chronology = i.getChronology();
		copyNumber = i.getCopyNumber(); 
		//copyNumberLabel = i.getCopyNumberLabel(); 
		//volumeNumber = i.getVolumeNumber();
		//volumeNumberLabel = i.getVolumeNumberLabel();
		fund = i.getFund();
		//donorPublicDisplay = i.getDonorPublicDisplay();
		//donorNote = i.getDonorNote();
		itemDonor = i.getItemDonor();
		price = i.getPrice();
		numberOfPieces = i.getNumberOfPieces();
		itemStatus = i.getItemStatus();
		itemStatusDateUpdated = i.getItemStatusDateUpdated();
		checkinNote = i.getCheckinNote();
		staffOnlyFlag = i.getStaffOnlyFlag();
		fastAddFlag = i.getFastAddFlag();
		this.accessInformation = i.getAccessInformation();
		this.itemHoldings = i.getItemHoldings();
		this.claimsReturnedFlag = i.getClaimsReturnedFlag();
		this.claimsReturnedFlagCreateDate = i.getClaimsReturnedFlagCreateDate();
		this.claimsReturnedNote = i.getClaimsReturnedNote();
		this.uniqueIdPrefix = i.getUniqueIdPrefix();
	}
	
	@ManyToOne
	@JoinColumn(name="HOLDINGS_ID")
	public OLEHoldings getItemHoldings() {
		return this.itemHoldings;
	}
	public void setItemHoldings(OLEHoldings holdings) { 
		this.itemHoldings = holdings;
	}
	
	// Also gone from the data model
	/*
	public String getAnalytic() {
		return analytic;
	}

	public void setAnalytic(String analytic) {
		this.analytic = analytic;
	}
	

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}
	*/
	
	@Column(name="CLAIMS_RETURNED")
	public String getClaimsReturnedFlag() {
		return claimsReturnedFlag;
	}

	public void setClaimsReturnedFlag(String claimsReturnedFlag) {
		this.claimsReturnedFlag = claimsReturnedFlag;
	}

	@Column(name="CLAIMS_RETURNED_DATE_CREATED")
	public String getClaimsReturnedFlagCreateDate() {
		return claimsReturnedFlagCreateDate;
	}

	public void setClaimsReturnedFlagCreateDate(String claimsReturnedFlagCreateDate) {
		this.claimsReturnedFlagCreateDate = claimsReturnedFlagCreateDate;
	}

	@Column(name="CLAIMS_RETURNED_NOTE")
	public String getClaimsReturnedNote() {
		return claimsReturnedNote;
	}

	public void setClaimsReturnedNote(String claimsReturnedNote) {
		this.claimsReturnedNote = claimsReturnedNote;
	}

	@Column(name="CURRENT_BORROWER")
	public String getCurrentBorrower() {
		return currentBorrower;
	}

	public void setCurrentBorrower(String currentBorrower) {
		this.currentBorrower = currentBorrower;
	}

	@Column(name="PROXY_BORROWER")
	public String getProxyBorrower() {
		return proxyBorrower;
	}

	public void setProxyBorrower(String proxyBorrower) {
		this.proxyBorrower = proxyBorrower;
	}

	@Column(name="DUE_DATE_TIME")
	public String getDueDateTime() {
		return dueDateTime;
	}

	public void setDueDateTime(String dueDateTime) {
		this.dueDateTime = dueDateTime;
	}

	@Column(name="ITEM_DAMAGED_STATUS")
	public String getItemDamagedStatus() {
		return itemDmgStatus;
	}

	public void setItemDamagedStatus(String itemDmgStatus) {
		this.itemDmgStatus = itemDmgStatus;
	}

	@Column(name="ITEM_DAMAGED_NOTE")
	public String getItemDamagedNote() {
		return itemDmgNote;
	}

	public void setItemDamagedNote(String itemDmgNote) {
		this.itemDmgNote = itemDmgNote;
	}

	@Column(name="MISSING_PIECES")
	public String getMissingPieces() {
		return missingPieces;
	}

	public void setMissingPieces(String itemMissingPicsFlag) {
		this.missingPieces = itemMissingPicsFlag;
	}

	@Column(name="MISSING_PIECES_NOTE")
	public String getMissingPiecesNote() {
		return missingPiecesNote;
	}

	public void setMissingPiecesNote(String missingPicsNote) {
		this.missingPiecesNote = missingPicsNote;
	}

	@Column(name="MISSING_PIECES_EFFECTIVE_DATE")
	public String getMissingPiecesEffectDate() {
		return missingPiecesEffectDate;
	}

	public void setMissingPiecesEffectDate(String missingPicsEffectDate) {
		this.missingPiecesEffectDate = missingPicsEffectDate;
	}

	@Column(name="MISSING_PIECES_COUNT")
	public Long getMissingPiecesCount() {
		return missingPiecesCount;
	}

	public void setMissingPiecesCount(Long missingPicsCount) {
		this.missingPiecesCount = missingPicsCount;
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
	/*
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
	*/
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="item")
	@XmlElement(name="note")
	public List<ItemNote> getNotes() {
		return notes;
	}

	public void setNotes(List<ItemNote> notes) {
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
	//@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="HIGH_DENSITY_STORAGE_ID")
	@XmlElement(name="highDensityStorage")
	public HighDensityStorage getHighDensityStorage() {
		return highDensityStorage;
	}

	public void setHighDensityStorage(HighDensityStorage highDensityStorage) {
		this.highDensityStorage = highDensityStorage;
	}

	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="TEMP_ITEM_TYPE_ID", referencedColumnName="ITM_TYP_CD_ID")
	public Deliver_ItemType getTemporaryItemType() {
		return temporaryItemType;
	}

	//public void setTemporaryItemType(TemporaryItemType temporaryItemType) {
	public void setTemporaryItemType(Deliver_ItemType temporaryItemType) {
		this.temporaryItemType = temporaryItemType;
	}

	public void setTemporaryItemType(String code, String name) {
		Deliver_ItemType type;
		//TypedQuery<ItemType> query = LU_DBLoadInstances.em.createQuery("SELECT t FROM ItemType t WHERE t.code='" + code + "'", ItemType.class);
		TypedQuery<Deliver_ItemType> query = LU_DBLoadInstances.ole_em.createQuery("SELECT t FROM Deliver_ItemType t WHERE t.code='" + code + "'", Deliver_ItemType.class);
		query.setHint("org.hibernate.cacheable", true);
		List<Deliver_ItemType> results = query.getResultList();
		if ( results.size() == 0 ) {
			//System.out.println("Creating new item type with code " + code);
			type = new Deliver_ItemType(code, name);
		} else {
			type = results.get(0);
			//System.out.println("Fetched existing item type with code " + type.getCode());
		}		
		this.setTemporaryItemType(type);
	}
	
	@Column(name="FUND")
	@XmlElement(name="fund", required=true, nillable=true)
	public String getFund() {
		return fund;
	}

	public void setFund(String fund) {
		this.fund = fund;
	}

	@OneToOne(fetch=FetchType.LAZY, mappedBy="item", cascade=CascadeType.ALL)
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
	public double getPrice() {
		return price;
	}

	public void setPrice(double newprice) {
		this.price = newprice;
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

	/*
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="ITEM_STATUS_ID")
	@XmlElement(name="itemStatus", required=true, nillable=true)
	public ItemStatus getItemStatus() {
		return itemStatus;
	}
	public void setItemStatus(ItemStatus itemStatus) {
		this.itemStatus = itemStatus;
	}
	public void setItemStatus(String code, String name) {
		ItemStatus status;
		// TypedQuery<ItemStatus> query = LU_DBLoadInstances.em.createQuery("SELECT s FROM ItemStatus s WHERE s.code='" + code + "'", ItemStatus.class);
		TypedQuery<ItemStatus> query = LU_DBLoadInstances.ole_em.createQuery("SELECT s FROM ItemStatus s WHERE s.deliverStatus.code='" + code + "'", ItemStatus.class);
		query.setHint("org.hibernate.cacheable", true);
		List<ItemStatus> results = query.getResultList();
		if ( results.size() == 0 ) {
			status = new ItemStatus(code, name);
		} else {
			status = results.get(0);
		}		
		this.setItemStatus(status);
	}
	*/
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="ITEM_STATUS_ID", referencedColumnName="ITEM_AVAIL_STAT_ID")
	public Deliver_ItemStatus getItemStatus() {
		return itemStatus;
	}
	public void setItemStatus(Deliver_ItemStatus itemStatus) {
		this.itemStatus = itemStatus;
	}
	public void setItemStatus(String code, String name) {
		Deliver_ItemStatus status;
		// TypedQuery<ItemStatus> query = LU_DBLoadInstances.em.createQuery("SELECT s FROM ItemStatus s WHERE s.code='" + code + "'", ItemStatus.class);
		TypedQuery<Deliver_ItemStatus> query = LU_DBLoadInstances.ole_em.createQuery("SELECT s FROM Deliver_ItemStatus s WHERE s.code='" + code + "'", Deliver_ItemStatus.class);
		query.setHint("org.hibernate.cacheable", true);
		List<Deliver_ItemStatus> results = query.getResultList();
		if ( results.size() == 0 ) {
			status = new Deliver_ItemStatus(code, name);
		} else {
			status = results.get(0);
		}		
		this.setItemStatus(status);
	}
	
	// I'm assuming the database's "EFFECTIVE_DATE" field refers to the item status ...
	@Column(name="ITEM_STATUS_DATE_UPDATED")
	@XmlElement(name="itemStatusEffectiveDate", required=true, nillable=true)
	public String getItemStatusDateUpdated() {
		return itemStatusDateUpdated;
	}

	public void setItemStatusDateUpdated(String itemStatusDate) {
		this.itemStatusDateUpdated = itemStatusDate;
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
	public FlatLocation getLocation() {
		return location;
	}
	
	public void setLocation(FlatLocation location) {
		this.location = location;
	}
	
	/*
	@Column(name="LOCATION_LEVEL")
	public String getLocationLevel() {
		return this.locationLevel;
	}
	
	public void setLocationLevel(String level) { 
		this.locationLevel = level;
	}
	*/
	
	@Embedded
	@XmlElement(name="callNumber", required=true, nillable=true)
	public CallNumber getCallNumber() {
		return callNumber;
	}
	public void setCallNumber(CallNumber callNumber) {
		this.callNumber = callNumber;
	}

	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="CALL_NUMBER_TYPE_ID", referencedColumnName="SHVLG_SCHM_ID")
	public CallNumberType getCallNumberType() {
		return callNumberType;
	}
	public void setCallNumberType(CallNumberType callNumberType) {
		this.callNumberType = callNumberType;
	}
	public void setCallNumberType(String code, String name) {
		CallNumberType type;
		TypedQuery<CallNumberType> query = LU_DBLoadInstances.ole_em.createQuery("SELECT t FROM CallNumberType t WHERE t.code='" + code + "'", CallNumberType.class);
		query.setHint("org.hibernate.cacheable", true);
		List<CallNumberType> results = query.getResultList();
		if ( results.size() == 0 ) {
			type = new CallNumberType();
			type.setCode(code);
			type.setName(name);
		} else {
			type = results.get(0);
		}		
		this.setCallNumberType(type);
	}

	/*
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="ITEM_TYPE_ID")
	@XmlElement(name="itemType")
	public ItemType getItemType() {
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}
	public void setItemType(String code, String name) {
		ItemType type;
		//TypedQuery<ItemType> query = LU_DBLoadInstances.em.createQuery("SELECT t FROM ItemType t WHERE t.code='" + code + "'", ItemType.class);
		TypedQuery<ItemType> query = LU_DBLoadInstances.ole_em.createQuery("SELECT t FROM ItemType t WHERE t.deliverType.code='" + code + "'", ItemType.class);
		query.setHint("org.hibernate.cacheable", true);
		List<ItemType> results = query.getResultList();
		if ( results.size() == 0 ) {
			//System.out.println("Creating new item type with code " + code);
			type = new ItemType(code, name);
		} else {
			type = results.get(0);
			//System.out.println("Fetched existing item type with code " + type.getCode());
		}		
		this.setItemType(type);
	}
	*/
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="ITEM_TYPE_ID", referencedColumnName="ITM_TYP_CD_ID")
	public Deliver_ItemType getItemType() {
		return itemType;
	}

	public void setItemType(Deliver_ItemType itemType) {
		this.itemType = itemType;
	}
	public void setItemType(String code, String name) {
		Deliver_ItemType type;
		//TypedQuery<ItemType> query = LU_DBLoadInstances.em.createQuery("SELECT t FROM ItemType t WHERE t.code='" + code + "'", ItemType.class);
		TypedQuery<Deliver_ItemType> query = LU_DBLoadInstances.ole_em.createQuery("SELECT t FROM Deliver_ItemType t WHERE t.code='" + code + "'", Deliver_ItemType.class);
		query.setHint("org.hibernate.cacheable", true);
		List<Deliver_ItemType> results = query.getResultList();
		if ( results.size() == 0 ) {
			//System.out.println("Creating new item type with code " + code);
			type = new Deliver_ItemType(code, name);
		} else {
			type = results.get(0);
			//System.out.println("Fetched existing item type with code " + type.getCode());
		}		
		this.setItemType(type);
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
	//@OneToMany(fetch=FetchType.LAZY, mappedBy="item")
	@XmlElement(name="formerIdentifier")
	public List<FormerIdentifier> getFormerIdentifiers() {
		return formerIdentifiers;
	}

	public void setFormerIdentifiers(List<FormerIdentifier> formerIdentifiers) {
		this.formerIdentifiers = formerIdentifiers;
	}

	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="item")
	//@OneToMany(fetch=FetchType.LAZY)
	//@JoinColumn(name="ITEM_ID")
	@XmlElement(name="statisticalSearchingCode")
	public List<ItemStatSearch> getStatSearches() {
		return statSearches;
	}

	public void setStatSearches(List<ItemStatSearch> statSearches) {
		this.statSearches = statSearches;
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
	
	
	@XmlAttribute(name="resourceIdentifier")
	public String getResourceIdentifier() {
		return resourceIdentifier;
	}

	public void setResourceIdentifier(String resourceIdentifier) {
		this.resourceIdentifier = resourceIdentifier;
	}
	*/
	
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

	@Column(name="UNIQUE_ID_PREFIX")
	public String getUniqueIdPrefix() {
		return uniqueIdPrefix;
	}
	public void setUniqueIdPrefix(String uniqueIdPrefix) {
		this.uniqueIdPrefix = uniqueIdPrefix;
	}

	@Column(name="DATE_CREATED")
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String datestr = df.format(Calendar.getInstance().getTime());
		try {
			df.parse(createdDate);
			// if there's no exception, then it's fine, assign the date created
			this.createdDate = createdDate;
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set item record's created date from date string: " + createdDate,
					LU_DBLoadInstances.LOG_ERROR);
			this.createdDate = datestr;
		}		
	}

	@Column(name="CREATED_BY")
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Column(name="DATE_UPDATED")
	public String getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(String updateDate) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String datestr = df.format(Calendar.getInstance().getTime());
		try {
			df.parse(updateDate);
			// if there's no exception, then it's fine, assign the date created
			this.updatedDate = updateDate;
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set item record's updated date from date string: " + updateDate,
					LU_DBLoadInstances.LOG_ERROR);
			this.updatedDate = datestr;
		}		
	}

	@Column(name="UPDATED_BY")
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}
