package migration;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="serials")
public class Serial implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7863677445498499793L;

	@Id
	@Column(name="id")
	private Long id;
	
	@Column(name="bibid")
	private String bibid; // bibid in Sirsi -- former identifier in OLE, but padded out to 8 characters 
	                      // and 10000000 added, so 1203539 becomes 11203539
	@Column(name="SERC_ID")
	private String serialControlId;
	@Column(name="SERC_LIB")
	private String library;
	@Column(name="SERC_TITLE_KEY")
	private String titleKey; // This is what we used to retrieve bib id
	@Column(name="BASE_CALLNUM")
	private String baseCallNumber; // use this ^^ to look up holdings for creating .csv to load into OLE
	@Column(name="CLASS")
	private String callNumberClass;
	@Column(name="SISAC_ID")
	private String sisacID; // no info on what this is in Sirsi manual
	@Column(name="VEND_SUB_ID")
	private String vendorSubscriptionId;
	@Column(name="SERC_CAT1")
	private String category1;
	@Column(name="SERC_CAT2")
	private String category2;
	@Column(name="SERC_STATUS")
	private String subscriptionStatus;
	@Column(name="SERC_DATE_CREATED")
	private String dateCreated;
	@Column(name="SERC_USE1")
	private String useSubdiv1;
	@Column(name="SERC_USE2")
	private String useSubdiv2;
	@Column(name="SERC_USE3")
	private String useSubdiv3;
	@Column(name="SERC_LBL1")
	private String labelSubdiv1;
	@Column(name="SERC_LBL2")
	private String labelSubdiv2;
	@Column(name="SERC_LBL3")
	private String labelSubdiv3;
	@Column(name="SERC_NXT1")
	private String nextSubdiv1;
	@Column(name="SERC_NXT2")
	private String nextSubdiv2;
	@Column(name="SERC_NXT3")
	private String nextSubdiv3;
	@Column(name="SERC_CNT1")
	private String continueSubdiv1;
	@Column(name="SERC_CNT2")
	private String continueSubdiv2;
	@Column(name="SERC_CNT3")
	private String continueSubdiv3;
	@Column(name="SERC_FRM1")
	private String formSubdiv1;
	@Column(name="SERC_FRM2")
	private String formSubdiv2;
	@Column(name="SERC_FRM3")
	private String formSubdiv3;
	@Column(name="SERC_LMT1")
	private String limitSubdiv1;
	@Column(name="SERC_LMT2")
	private String limitSubdiv2;
	@Column(name="SERC_LMT3")
	private String limitSubdiv3;
	@Column(name="NAM_TYPE")
	private String nameType;
	@Column(name="CLM_PD")
	private String claimPeriod;
	@Column(name="SUBSEQ_CLM_PD")
	private String subsequentClaimPeriod;
	@Column(name="DISPLAY_IN_CATALOG")
	private String numIssuesToDisplay;
	@Column(name="CAT_NAM")
	private String catalogName;
	@Column(name="DATE_PUBLISHED")
	private String dateNextIssuePublished;
	@Column(name="ADD_HLD_CAT")
	private String autoUpdateMFHDRec;
	@Column(name="SERC_REC_COP")
	private String copiesToReceive;
	@Column(name="SUB_EXPD")
	private String subscriptionExpireDate;
	@Column(name="ORDER_ID")
	private String linkedOrderId;
	@Column(name="LINE_ITEM_NUMBER")
	private String linkedOrderLineItemNumber;
	@Column(name="FISC_CYC")
	private String linkedOrderFiscalCycle;
	@Column(name="VENDOR_ID")
	private String linkedVendorId;
	@Column(name="VEND_LIBR")
	private String linkedVendorLibrary;
	@Column(name="PREDICT_ISSUES")
	private String predictIssues;
	@Column(name="PUBCYCLE_DEF")
	private String publicationCycleDefinition;
	@Column(name="REC_CYC")
	private String receiptCycle;
	@Column(name="SERC_CYC")
	private String cycleType;
	@Column(name="POS_NCKIN")
	private String positionofFutureCheckin;
	@Column(name="REC_DATE")
	private String nextIssueExpectedDate;
	@Column(name="SERC_NAM")
	private String nextIssueExpectedName;
	@Column(name="VEND_TITL_NUM")
	private String vendorTitleNumber;
	@Column(name="CUSTOM_NAMES")
	private String customIssueNames;
	@Column(name="SUB_ISS")
	private String numberIssuesInSubscription;
	@Column(name="BND_NUM")
	private String numberToBind;
	@Column(name="BND_DAT")
	private String dateToBind;
	@Column(name="BND_DAT_SENT")
	private String dateSentToBind;
	@Column(name="HOLDING_CODE")
	private String holdingCode;
	@Column(name="UPD_MFHL")
	private String updateHoldings;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="serial", cascade=CascadeType.ALL)
	private List<SerialName> serial_names;
	@OneToMany(fetch=FetchType.LAZY, mappedBy="serial", cascade=CascadeType.ALL)
	private List<SerialNote> serial_notes;
	@OneToMany(fetch=FetchType.LAZY, mappedBy="serial", cascade=CascadeType.ALL)
	private List<SerialPhysform> serial_physforms;

	public Serial() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBibid() {
		return bibid;
	}

	public void setBibid(String bibid) {
		this.bibid = bibid;
	}

	public String getSerialControlId() {
		return serialControlId;
	}

	public void setSerialControlId(String ctlId) {
		this.serialControlId = ctlId;
	}

	public String getLibrary() {
		return library;
	}

	public void setLibrary(String library) {
		this.library = library;
	}

	public String getTitleKey() {
		return titleKey;
	}

	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}

	public String getBaseCallNumber() {
		return baseCallNumber;
	}

	public void setBaseCallNumber(String baseCallNumber) {
		this.baseCallNumber = baseCallNumber;
	}

	public String getCallNumberClass() {
		return callNumberClass;
	}

	public void setCallNumberClass(String callNumberClass) {
		this.callNumberClass = callNumberClass;
	}

	public String getSisacID() {
		return sisacID;
	}

	public void setSisacID(String sisacID) {
		this.sisacID = sisacID;
	}

	public String getVendorSubscriptionId() {
		return vendorSubscriptionId;
	}

	public void setVendorSubscriptionId(String vendorSubscriptionId) {
		this.vendorSubscriptionId = vendorSubscriptionId;
	}

	public String getCategory1() {
		return category1;
	}

	public void setCategory1(String category1) {
		this.category1 = category1;
	}

	public String getCategory2() {
		return category2;
	}

	public void setCategory2(String category2) {
		this.category2 = category2;
	}

	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getUseSubdiv1() {
		return useSubdiv1;
	}

	public void setUseSubdiv1(String useSubdiv1) {
		this.useSubdiv1 = useSubdiv1;
	}

	public String getUseSubdiv2() {
		return useSubdiv2;
	}

	public void setUseSubdiv2(String useSubdiv2) {
		this.useSubdiv2 = useSubdiv2;
	}

	public String getUseSubdiv3() {
		return useSubdiv3;
	}

	public void setUseSubdiv3(String useSubdiv3) {
		this.useSubdiv3 = useSubdiv3;
	}

	public String getLabelSubdiv1() {
		return labelSubdiv1;
	}

	public void setLabelSubdiv1(String labelSubdiv1) {
		this.labelSubdiv1 = labelSubdiv1;
	}

	public String getLabelSubdiv2() {
		return labelSubdiv2;
	}

	public void setLabelSubdiv2(String labelSubdiv2) {
		this.labelSubdiv2 = labelSubdiv2;
	}

	public String getLabelSubdiv3() {
		return labelSubdiv3;
	}

	public void setLabelSubdiv3(String labelSubdiv3) {
		this.labelSubdiv3 = labelSubdiv3;
	}

	public String getNextSubdiv1() {
		return nextSubdiv1;
	}

	public void setNextSubdiv1(String nextSubdiv1) {
		this.nextSubdiv1 = nextSubdiv1;
	}

	public String getNextSubdiv2() {
		return nextSubdiv2;
	}

	public void setNextSubdiv2(String nextSubdiv2) {
		this.nextSubdiv2 = nextSubdiv2;
	}

	public String getNextSubdiv3() {
		return nextSubdiv3;
	}

	public void setNextSubdiv3(String nextSubdiv3) {
		this.nextSubdiv3 = nextSubdiv3;
	}

	public String getContinueSubdiv1() {
		return continueSubdiv1;
	}

	public void setContinueSubdiv1(String continueSubdiv1) {
		this.continueSubdiv1 = continueSubdiv1;
	}

	public String getContinueSubdiv2() {
		return continueSubdiv2;
	}

	public void setContinueSubdiv2(String continueSubdiv2) {
		this.continueSubdiv2 = continueSubdiv2;
	}

	public String getContinueSubdiv3() {
		return continueSubdiv3;
	}

	public void setContinueSubdiv3(String continueSubdiv3) {
		this.continueSubdiv3 = continueSubdiv3;
	}

	public String getFormSubdiv1() {
		return formSubdiv1;
	}

	public void setFormSubdiv1(String formSubdiv1) {
		this.formSubdiv1 = formSubdiv1;
	}

	public String getFormSubdiv2() {
		return formSubdiv2;
	}

	public void setFormSubdiv2(String formSubdiv2) {
		this.formSubdiv2 = formSubdiv2;
	}

	public String getFormSubdiv3() {
		return formSubdiv3;
	}

	public void setFormSubdiv3(String formSubdiv3) {
		this.formSubdiv3 = formSubdiv3;
	}

	public String getLimitSubdiv1() {
		return limitSubdiv1;
	}

	public void setLimitSubdiv1(String limitSubdiv1) {
		this.limitSubdiv1 = limitSubdiv1;
	}

	public String getLimitSubdiv2() {
		return limitSubdiv2;
	}

	public void setLimitSubdiv2(String limitSubdiv2) {
		this.limitSubdiv2 = limitSubdiv2;
	}

	public String getLimitSubdiv3() {
		return limitSubdiv3;
	}

	public void setLimitSubdiv3(String limitSubdiv3) {
		this.limitSubdiv3 = limitSubdiv3;
	}

	public String getNameType() {
		return nameType;
	}

	public void setNameType(String nameType) {
		this.nameType = nameType;
	}

	public String getClaimPeriod() {
		return claimPeriod;
	}

	public void setClaimPeriod(String claimPeriod) {
		this.claimPeriod = claimPeriod;
	}

	public String getSubsequentClaimPeriod() {
		return subsequentClaimPeriod;
	}

	public void setSubsequentClaimPeriod(String subsequentClaimPeriod) {
		this.subsequentClaimPeriod = subsequentClaimPeriod;
	}

	public String getNumIssuesToDisplay() {
		return numIssuesToDisplay;
	}

	public void setNumIssuesToDisplay(String numIssuesToDisplay) {
		this.numIssuesToDisplay = numIssuesToDisplay;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	public String getDateNextIssuePublished() {
		return dateNextIssuePublished;
	}

	public void setDateNextIssuePublished(String dateNextIssuePublished) {
		this.dateNextIssuePublished = dateNextIssuePublished;
	}

	public String getAutoUpdateMFHDRec() {
		return autoUpdateMFHDRec;
	}

	public void setAutoUpdateMFHDRec(String autoUpdateMFHDRec) {
		this.autoUpdateMFHDRec = autoUpdateMFHDRec;
	}

	public String getCopiesToReceive() {
		return copiesToReceive;
	}

	public void setCopiesToReceive(String copiesToReceive) {
		this.copiesToReceive = copiesToReceive;
	}

	public String getSubscriptionExpireDate() {
		return subscriptionExpireDate;
	}

	public void setSubscriptionExpireDate(String subscriptionExpireDate) {
		this.subscriptionExpireDate = subscriptionExpireDate;
	}

	public String getLinkedOrderId() {
		return linkedOrderId;
	}

	public void setLinkedOrderId(String linkedOrderId) {
		this.linkedOrderId = linkedOrderId;
	}

	public String getLinkedOrderLineItemNumber() {
		return linkedOrderLineItemNumber;
	}

	public void setLinkedOrderLineItemNumber(String linkedOrderLineItemNumber) {
		this.linkedOrderLineItemNumber = linkedOrderLineItemNumber;
	}

	public String getLinkedOrderFiscalCycle() {
		return linkedOrderFiscalCycle;
	}

	public void setLinkedOrderFiscalCycle(String linkedOrderFiscalCycle) {
		this.linkedOrderFiscalCycle = linkedOrderFiscalCycle;
	}

	public String getLinkedVendorId() {
		return linkedVendorId;
	}

	public void setLinkedVendorId(String linkedVendorId) {
		this.linkedVendorId = linkedVendorId;
	}

	public String getLinkedVendorLibrary() {
		return linkedVendorLibrary;
	}

	public void setLinkedVendorLibrary(String linkedVendorLibrary) {
		this.linkedVendorLibrary = linkedVendorLibrary;
	}

	public String getPredictIssues() {
		return predictIssues;
	}

	public void setPredictIssues(String predictIssues) {
		this.predictIssues = predictIssues;
	}

	public String getPublicationCycleDefinition() {
		return publicationCycleDefinition;
	}

	public void setPublicationCycleDefinition(String publicationCycleDefinition) {
		this.publicationCycleDefinition = publicationCycleDefinition;
	}

	public String getReceiptCycle() {
		return receiptCycle;
	}

	public void setReceiptCycle(String receiptCycle) {
		this.receiptCycle = receiptCycle;
	}

	public String getCycleType() {
		return cycleType;
	}

	public void setCycleType(String cycleType) {
		this.cycleType = cycleType;
	}

	public String getPositionofFutureCheckin() {
		return positionofFutureCheckin;
	}

	public void setPositionofFutureCheckin(String positionofFutureCheckin) {
		this.positionofFutureCheckin = positionofFutureCheckin;
	}

	public String getNextIssueExpectedDate() {
		return nextIssueExpectedDate;
	}

	public void setNextIssueExpectedDate(String nextIssueExpectedDate) {
		this.nextIssueExpectedDate = nextIssueExpectedDate;
	}

	public String getNextIssueExpectedName() {
		return nextIssueExpectedName;
	}

	public void setNextIssueExpectedName(String nextIssueExpectedName) {
		this.nextIssueExpectedName = nextIssueExpectedName;
	}

	public String getVendorTitleNumber() {
		return vendorTitleNumber;
	}

	public void setVendorTitleNumber(String vendorTitleNumber) {
		this.vendorTitleNumber = vendorTitleNumber;
	}

	public String getCustomIssueNames() {
		return customIssueNames;
	}

	public void setCustomIssueNames(String customIssueNames) {
		this.customIssueNames = customIssueNames;
	}

	public String getNumberIssuesInSubscription() {
		return numberIssuesInSubscription;
	}

	public void setNumberIssuesInSubscription(String numberIssuesInSubscription) {
		this.numberIssuesInSubscription = numberIssuesInSubscription;
	}

	public String getNumberToBind() {
		return numberToBind;
	}

	public void setNumberToBind(String numberToBind) {
		this.numberToBind = numberToBind;
	}

	public String getDateToBind() {
		return dateToBind;
	}

	public void setDateToBind(String dateToBind) {
		this.dateToBind = dateToBind;
	}

	public String getDateSentToBind() {
		return dateSentToBind;
	}

	public void setDateSentToBind(String dateSentToBind) {
		this.dateSentToBind = dateSentToBind;
	}

	public String getHoldingCode() {
		return holdingCode;
	}

	public void setHoldingCode(String holdingCode) {
		this.holdingCode = holdingCode;
	}

	public String getUpdateHoldings() {
		return updateHoldings;
	}

	public void setUpdateHoldings(String updateHoldings) {
		this.updateHoldings = updateHoldings;
	}

	public List<SerialName> getSerial_names() {
		return serial_names;
	}

	public void setSerial_names(List<SerialName> serial_names) {
		this.serial_names = serial_names;
	}

	public List<SerialNote> getSerial_notes() {
		return serial_notes;
	}

	public void setSerial_notes(List<SerialNote> serial_notes) {
		this.serial_notes = serial_notes;
	}

	public List<SerialPhysform> getSerial_physforms() {
		return serial_physforms;
	}

	public void setSerial_physforms(List<SerialPhysform> serial_physforms) {
		this.serial_physforms = serial_physforms;
	}
	
	
}
