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
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.lu.oleconvert.LU_DBLoadInstances;

@Entity
@Table(name="ole_ds_holdings_t")
@XmlType(name="oleHoldings", propOrder={"holdingsIdentifier", "receiptStatus", "uri", "notes", "location", "callNumber", "extentOfOwnership" })
public class OLEHoldings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7571390668933186865L;

	//private String primary;
	private Long holdingsIdentifier;
	private ReceiptStatus receiptStatus;
	//private ArrayList<URI> uri;
	private List<AccessURI> accessURIs;
	private List<OLEHoldingsNote> notes;
	// Eventually this will be an entity, for now it's still embedded
	//private Location location;
	private String locationStr;
	private String locationLevelStr;
	private CallNumber callNumber;
	private CallNumberType callNumberType;
	private List<ExtentOfOwnership> extentOfOwnership;
	private Instance instance;
	private Bib bib;
	private Long bibId;
	private List<Item> items;
	private String copyNumber;
	private String publisher;
	private String createdDate;
	private String createdBy;
	private String updatedDate;
	private String updatedBy;
	private String platform;
	private String accessStatus;
	private String accessStatusDateUpdated;
	private String imprint;
	private List<OLEHoldingsStatSearch> statSearch;
	private String localPersistentURI;
	private String subscriptionStatus;
	private String adminUserName;
	private String adminPassword;
	private String adminUrl;
	//private String link;
	//private String linkText;
	private String allowILL;
	private Long authTypeID;
	private String proxiedResource;
	private String numSimultaneousUser;
	private List<OLEHoldingsAccessLocation> oleHoldingsAccessLocations;
	private String accessUserName;
	private String accessPassword;
	private String eResourceId;
	private String sourceHoldingsContent;
	private String staffOnly;
	private String holdingsType;
	private List<Coverage> coverage;
	private String uniqueIdPrefix;
	
	public OLEHoldings() {
		super();
		accessURIs = new ArrayList<AccessURI>();
		extentOfOwnership = new ArrayList<ExtentOfOwnership>();
		extentOfOwnership = new ArrayList<ExtentOfOwnership>();
		statSearch = new ArrayList<OLEHoldingsStatSearch>();
		oleHoldingsAccessLocations = new ArrayList<OLEHoldingsAccessLocation>();
		items = new ArrayList<Item>();
		notes = new ArrayList<OLEHoldingsNote>();
		this.coverage = new ArrayList<Coverage>();
		// constructing this prematurely causes tons of nulls to be inserted into the database
		//callNumberType = new CallNumberType();
		instance = null;
		allowILL = "Y";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String datestr = df.format(Calendar.getInstance().getTime());
		this.setCreatedDate(datestr);
		this.setCreatedBy("BulkIngest-User");
		this.setUpdatedDate(datestr);
		this.setUpdatedBy("BulkIngest-User");
		this.setAccessStatus("open"); // TODO: what to put in this field?
		this.setAccessStatusDateUpdated(datestr);
		this.uniqueIdPrefix = "who";
	}

	public OLEHoldings(Bib bib) {
		this();
	   	this.setBib(bib);
    	this.setBibId(bib.getId());
	}
	public OLEHoldings(Instance i) {
		this();
		this.setInstance(i);
	}
	
	@Column(name="UNIQUE_ID_PREFIX")
	public String getUniqueIdPrefix() {
		return this.uniqueIdPrefix;
	}
	public void setUniqueIdPrefix(String pref) {
		this.uniqueIdPrefix = pref;
	}
	
	@Column(name="STAFF_ONLY")
	public String getStaffOnly() {
		return this.staffOnly;
	}
	public void setStaffOnly(String staffOnly) {
		this.staffOnly = staffOnly;
	}
	
	@Column(name="LOCATION")
	public String getLocationStr() {
		return locationStr;
	}
	public void setLocationStr(String locationStr) {
		this.locationStr = locationStr;
	}

	@Column(name="LOCATION_LEVEL")
	public String getLocationLevelStr() {
		return locationLevelStr;
	}
	public void setLocationLevelStr(String locationLevelStr) {
		this.locationLevelStr = locationLevelStr;
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
			LU_DBLoadInstances.Log(System.err, "Unable to set holdings record's created date from date string: " + createdDate,
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
		this.updatedDate = updateDate;
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String datestr = df.format(Calendar.getInstance().getTime());
		try {
			df.parse(updateDate);
			// if there's no exception, then it's fine, assign the date created
			this.updatedDate = updateDate;
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set holdings record's updated date from date string: " + updateDate,
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

	@Column(name="PLATFORM")
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}

	@Column(name="ACCESS_STATUS")
	public String getAccessStatus() {
		return accessStatus;
	}
	public void setAccessStatus(String accessStatus) {
		this.accessStatus = accessStatus;
	}

	@Column(name="ACCESS_STATUS_DATE_UPDATED")
	public String getAccessStatusDateUpdated() {
		return accessStatusDateUpdated;
	}
	public void setAccessStatusDateUpdated(String dateUpdated) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String datestr = df.format(Calendar.getInstance().getTime());
		try {
			df.parse(dateUpdated);
			// if there's no exception, then it's fine, assign the date created
			this.accessStatusDateUpdated = dateUpdated;
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set holdings record's created date from date string: " + createdDate,
					LU_DBLoadInstances.LOG_ERROR);
			this.accessStatusDateUpdated = datestr;
		}			
	}
	
	@Column(name="IMPRINT")
	public String getImprint() {
		return imprint;
	}
	public void setImprint(String imprint) {
		this.imprint = imprint;
	}

	@Column(name="LOCAL_PERSISTENT_URI")
	public String getLocalPersistentURI() {
		return localPersistentURI;
	}
	public void setLocalPersistentURI(String localPersistentURI) {
		this.localPersistentURI = localPersistentURI;
	}

	@Column(name="SUBSCRIPTION_STATUS")
	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}
	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	@Column(name="ADMIN_USERNAME")
	public String getAdminUserName() {
		return adminUserName;
	}
	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	@Column(name="ADMIN_PASSWORD")
	public String getAdminPassword() {
		return adminPassword;
	}
	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	@Column(name="ADMIN_URL")
	public String getAdminUrl() {
		return adminUrl;
	}
	public void setAdminUrl(String adminUrl) {
		this.adminUrl = adminUrl;
	}

	/*
	@Column(name="LINK")
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}

	@Column(name="LINK_TEXT")
	public String getLinkText() {
		return linkText;
	}
	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}
	*/
	
	@Column(name="ALLOW_ILL")
	public String getAllowILL() {
		return allowILL;
	}
	public void setAllowILL(String allowILL) {
		this.allowILL = allowILL;
	}

	@Column(name="AUTHENTICATION_TYPE_ID")
	public Long getAuthTypeID() {
		return authTypeID;
	}
	public void setAuthTypeID(Long authTypeID) {
		this.authTypeID = authTypeID;
	}

	@Column(name="PROXIED_RESOURCE")
	public String getProxiedResource() {
		return proxiedResource;
	}
	public void setProxiedResource(String proxiedResource) {
		this.proxiedResource = proxiedResource;
	}

	@Column(name="NUMBER_SIMULT_USERS")
	public String getNumSimultaneousUser() {
		return numSimultaneousUser;
	}
	public void setNumSimultaneousUser(String numSimultaneousUser) {
		this.numSimultaneousUser = numSimultaneousUser;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="oleHoldings", cascade=CascadeType.ALL)
	public List<OLEHoldingsAccessLocation> getOleHoldingsAccessLocation() {
		return oleHoldingsAccessLocations;
	}
	public void setOleHoldingsAccessLocation(
			List<OLEHoldingsAccessLocation> holdingsAccessLocations) {
		this.oleHoldingsAccessLocations = holdingsAccessLocations;
	}

	@Column(name="ACCESS_USERNAME")
	public String getAccessUserName() {
		return accessUserName;
	}
	public void setAccessUserName(String accessUserName) {
		this.accessUserName = accessUserName;
	}

	@Column(name="ACCESS_PASSWORD")
	public String getAccessPassword() {
		return accessPassword;
	}
	public void setAccessPassword(String accessPassword) {
		this.accessPassword = accessPassword;
	}

	@Column(name="E_RESOURCE_ID")
	public String geteResourceId() {
		return eResourceId;
	}
	public void seteResourceId(String eResourceId) {
		this.eResourceId = eResourceId;
	}

	@Column(name="SOURCE_HOLDINGS_CONTENT")
	public String getSourceHoldingsContent() {
		return sourceHoldingsContent;
	}
	public void setSourceHoldingsContent(String sourceHoldingsContent) {
		this.sourceHoldingsContent = sourceHoldingsContent;
	}

	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="oleHoldings")
	//@JoinColumn(name="HOLDINGS_STAT_SEARCH_ID")
	public List<OLEHoldingsStatSearch> getStatSearch() {
		return this.statSearch;
	}
	public void setStatSearch(List<OLEHoldingsStatSearch> statSearch) {
		this.statSearch = statSearch;
	}
	
	@Column(name="PUBLISHER")
	public String getPublisher() {
		return this.publisher;
	}
	public void setPublisher(String pub) {
		publisher = pub;
	}
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinTable(name="ole_ds_bib_holdings_t",
	   joinColumns={@JoinColumn(name="HOLDINGS_ID", referencedColumnName="HOLDINGS_ID")},
	   inverseJoinColumns={@JoinColumn(name="BIB_ID", referencedColumnName="BIB_ID")})	
	public Bib getBib() {
		return this.bib;
	}
	public void setBib(Bib bib) {
		this.bib = bib;
	}
	
	@Column(name="COPY_NUMBER")
	@XmlElement(name="copyNumber", required=true, nillable=true)
	public String getCopyNumber() {
		return copyNumber;
	}
	public void setCopyNumber(String copyNumber) {
		this.copyNumber = copyNumber;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="itemHoldings", cascade=CascadeType.ALL)
	//@OneToMany(fetch=FetchType.LAZY, mappedBy="instance")
	public List<Item> getItems() {
		return this.items;
	}
	public void setItems(List<Item> i) {
		this.items = i;
	}
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	//@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CALL_NUMBER_TYPE_ID")
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
	
	
	//@OneToOne
	//@JoinColumn(name="INSTANCE_ID")
	@Transient
	public Instance getInstance() {
		return this.instance;
	}
	public void setInstance(Instance i ) {
		this.instance = i;
	}
	
	// since I annotated the getters, the mappedBy field has to have what appears after "get"
	// in the getter method's signature
	@OneToMany(fetch=FetchType.LAZY, mappedBy="OLEHoldings", cascade=CascadeType.ALL)
	//@OneToMany(fetch=FetchType.LAZY, mappedBy="OLEHoldings")
	@XmlElement(name="extentOfOwnership")
	public List<ExtentOfOwnership> getExtentOfOwnership() {
		return extentOfOwnership;
	}

	public void setExtentOfOwnership(List<ExtentOfOwnership> extentOfOwnership) {
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
	
	/* Not in the data model, apparently
	@XmlAttribute(name="primary")
	public String getPrimary() {
		return primary;
	}
	public void setPrimary(String primary) {
		this.primary = primary;
	}
	*/
	
	@Id
	@GeneratedValue
	@Column(name="HOLDINGS_ID")
	@XmlElement(name="holdingsIdentifier")
	public Long getHoldingsIdentifier() {
		return holdingsIdentifier;
	}
	public void setHoldingsIdentifier(Long holdingsIdentifier) {
		this.holdingsIdentifier = holdingsIdentifier;
	}
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	//@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="RECEIPT_STATUS_ID")
	@XmlElement(name="receiptStatus")
	public ReceiptStatus getReceiptStatus() {
		return receiptStatus;
	}
	public void setReceiptStatus(ReceiptStatus recpeiptStatus) {
		this.receiptStatus = recpeiptStatus;
	}
	public void setReceiptStatus(String code, String name) {
		ReceiptStatus r;
		TypedQuery<ReceiptStatus> query = LU_DBLoadInstances.ole_em.createQuery("SELECT r FROM ReceiptStatus r WHERE r.code='" + code + "'", ReceiptStatus.class);
		query.setHint("org.hibernate.cacheable", true);
		List<ReceiptStatus> results = query.getResultList();
		if ( results.size() == 0 ) {
			r = new ReceiptStatus();
			r.setCode(code);
			r.setName(name);
		} else {
			r = results.get(0);
		}		
		this.setReceiptStatus(r);		
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="oleHoldings", cascade=CascadeType.ALL)
	//@OneToMany(fetch=FetchType.LAZY, mappedBy="oleHoldings")
	@XmlElement(name="uri")
	public List<AccessURI> getAccessURIs() {
		return accessURIs;
	}
	public void setAccessURIs(List<AccessURI> uris) {
		this.accessURIs = uris;
	}
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="OLEHoldings", cascade=CascadeType.ALL)
	//@OneToMany(fetch=FetchType.LAZY, mappedBy="OLEHoldings")
	@XmlElement(name="note")
	public List<OLEHoldingsNote> getNotes() {
		return notes;
	}
	public void setNotes(List<OLEHoldingsNote> notes) {
		this.notes = notes;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="OLEHoldings", cascade=CascadeType.ALL)
	public List<Coverage> getCoverage() {
		return this.coverage;
	}
	public void setCoverage(List<Coverage> coverage) {
		this.coverage = coverage;
		for ( Coverage c : this.coverage ) {
			c.setOLEHoldings(this);
		}
	}
	
	@Column(name="BIB_ID", nullable=false)
	public Long getBibId() {
		return bibId;
	}
	public void setBibId(Long bibId) {
		this.bibId = bibId;
	}

	@Column(name="HOLDINGS_TYPE")
	public String getHoldingsType() {
		return holdingsType;
	}
	public void setHoldingsType(String holdingsType) {
		this.holdingsType = holdingsType;
	}

	/*
	@OneToOne
	@JoinColumn(name="LOCATION_ID")
	public Location getLocation() {
		return location;
	}
	public void Location(Location location) {
		this.location = location;
	}
	*/
}
