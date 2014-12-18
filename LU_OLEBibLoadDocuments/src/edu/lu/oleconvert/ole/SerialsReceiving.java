package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.GenericGenerator;

import edu.lu.oleconvert.LU_DBLoadInstances;

@Entity
@Table(name="ole_ser_rcv_rec")
public class SerialsReceiving implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6061865930462198074L;

	// Chicago sets this to an ID that comes from Horizon.
	// We could set it to the SISAC_ID that comes from Sirsi, perhaps?
	// THat or the SERC_ID
	@Id
	@GeneratedValue // not generating this after all, we'll set it
	//@GeneratedValue(generator = "uuid")
	//@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name="SER_RCV_REC_ID")
	private Long id;
	
	/*
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="FDOC_NBR", referencedColumnName="DOC_HDR_ID")
	private FDoc fDoc;
	*/
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="serialsReceiving", cascade=CascadeType.ALL)
	List<SerialsReceivingHisRec> serialsReceivingHistory;
	
	public List<SerialsReceivingHisRec> getSerialsReceivingHistory() {
		return serialsReceivingHistory;
	}

	public void setSerialsReceivingHistory(
			List<SerialsReceivingHisRec> serialsReceivingHistory) {
		this.serialsReceivingHistory = serialsReceivingHistory;
	}

	@OneToOne(fetch=FetchType.LAZY, mappedBy="serialsReceiving", cascade=CascadeType.ALL)
	SerialsReceivingRecType serialsReceivingRecType;
	
	public SerialsReceivingRecType getSerialsReceivingRecType() {
		return serialsReceivingRecType;
	}

	public void setSerialsReceivingRecType(
			SerialsReceivingRecType serialsReceivingRecType) {
		this.serialsReceivingRecType = serialsReceivingRecType;
	}

	// set to 1 in all the Chicago data
	@Column(name="VER_NBR")
	private int versionNumber;
	
	// "Y" in all Chicago data
	@Column(name="ACTIVE")
	private String active;
	
	// set to same value as SER_RCV_REC_ID in Chicago data
	//@Id
	//@GeneratedValue // not generating this after all, we'll set it
	//@GeneratedValue(generator = "system-uuid")
	//@GenericGenerator(name="system-uuid", strategy = "uuid")
	@Column(name="OBJ_ID")
	private String objId;
	
	// null in Chicago data
	@Column(name="SUBSCR_STAT_DT")
	private String subscriptionStatusDate;
	
	// null in Chicago data, no explanation
	@Column(name="MACH_ID")
	private String machId;
	
	// use the same operator ID as the docstore loader
	@Column(name="OPTR_ID")
	private String operatorId;
	
	// null in Chicago data, maybe set to date of load
	@Column(name="CREATE_DATE")
	private String createDate;
	
	// all blank in Chicago data
	@Column(name="VENDOR")
	private String vendor;
	
	// all blank in Chicago data
	@Column(name="URGENT_NOTE")
	private String urgentNote;
	
	// in <INSTITUTION>/<LIBRARY>/<ShelvingLocation> format in Chicago's data
	@Column(name="UNBOUND_LOC")
	private String unboundLocation;
	
	@Column(name="TREATMENT_INSTR_NOTE")
	private String treatmentInstrNote;
	
	// "4" in all the Chicago data, no explanation of what the value means though
	@Column(name="SUBSCR_STAT")
	private String subscriptionStatus;
	
	// NULL in Chicago data, why a column with the same name as the entire table?
	@Column(name="SER_RCV_REC")
	private String receivingRec;
	
	// Looks like a circ desk or possibly library name
	@Column(name="SER_RCPT_LOC")
	private String receiptLocation;
	
	// "Y" in all the Chicago data
	@Column(name="PUBLIC_DISPLAY")
	private String publicDisplay;
	
	// "Y" in all the Chicago data
	@Column(name="PRINT_LBL")
	private String printLabel;
	
	// NULL in all Chicago data
	@Column(name="PO_ID")
	private String poId;
	
	@Column(name="BIB_ID")
	private String bibId;
	
	// NULL in all the Chicago data, not sure what to do with this
	//@Column(name="FDOC_NBR")
	//private String fDocNumber;
	
	// Y or N in Chicago data
	@Column(name="CLAIM")
	private String claim;
	
	// NULL in Chicago data
	@Column(name="GEN_RCV_NOTE")
	private String genReceivedNote;
	
	@Column(name="INSTANCE_ID")
	private String instanceId;
	
	// NULL in Chicago data
	@Column(name="CLAIM_INTRVL_INFO")
	private String claimIntervalInfo;
	
	// Y or N, all N in Chicago data
	@Column(name="CREATE_ITEM")
	private String createItem;
	
	// Apparently no forced relationship with the SerialsReceivingRecType table,
	// though SerialsReceivingRecTypes do each know about their Serials receiving record
	@Column(name="RCV_REC_TYP")
	private String recType;
	
	public SerialsReceiving() {
		super();
		this.versionNumber = 1; 
		this.setObjId(UUID.randomUUID().toString());
	}
	
	public SerialsReceiving(String serId) {
		this();
		this.setReceivingRec(serId);
		//this.fDoc = new FDoc(this);
		// cascade should handle persisting this
		//LU_BuildInstance.ole_em.persist(this.fDoc);
		
		//this.setId(serId);
		//this.setObjId(serId);
	}
	
	public String getBibId() {
		return this.bibId;
	}
	public void setBibId(String newid) {
		this.bibId = newid;
	}
	
	public String getInstanceId() {
		return this.instanceId;
	}
	public void setInstanceId(String newid) {
		this.instanceId = newid;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	public String getSubscriptionStatusDate() {
		return subscriptionStatusDate;
	}

	public void setSubscriptionStatusDate(String subscriptionStatusDate) {
		this.subscriptionStatusDate = subscriptionStatusDate;
	}

	public String getMachId() {
		return machId;
	}

	public void setMachId(String machId) {
		this.machId = machId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		String[] acceptedFormats = {"yyyyMMdd", "yyyy", "yyyy-MM-dd"};
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		try {
			Date newcreate = DateUtils.parseDate(createDate, acceptedFormats);
			// if there's no exception, then it's fine, assign the date created
			this.createDate = df.format(newcreate);
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set serials receiving record's create date from date string: " + createDate,
					LU_DBLoadInstances.LOG_ERROR);
			this.createDate = null;
		}
		LU_DBLoadInstances.Log(System.out, "Serials receiving record createDate set to: " + createDate,
				LU_DBLoadInstances.LOG_INFO);
		
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getUrgentNote() {
		return urgentNote;
	}

	public void setUrgentNote(String urgentNote) {
		this.urgentNote = urgentNote;
	}

	public String getUnboundLocation() {
		return unboundLocation;
	}

	public void setUnboundLocation(String unboundLocation) {
		this.unboundLocation = unboundLocation;
	}

	public String getTreatmentInstrNote() {
		return treatmentInstrNote;
	}

	public void setTreatmentInstrNote(String treatmentInstrNote) {
		this.treatmentInstrNote = treatmentInstrNote;
	}

	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	public String getReceivingRec() {
		return receivingRec;
	}

	public void setReceivingRec(String receivingRec) {
		this.receivingRec = receivingRec;
	}

	public String getReceiptLocation() {
		return receiptLocation;
	}

	public void setReceiptLocation(String receiptLocation) {
		this.receiptLocation = receiptLocation;
	}

	public String getPublicDisplay() {
		return publicDisplay;
	}

	public void setPublicDisplay(String publicDisplay) {
		this.publicDisplay = publicDisplay;
	}

	public String getPrintLabel() {
		return printLabel;
	}

	public void setPrintLabel(String printLabel) {
		this.printLabel = printLabel;
	}

	public String getPoId() {
		return poId;
	}

	public void setPoId(String poId) {
		this.poId = poId;
	}

	/*
	public FDoc getFDoc() {
		return fDoc;
	}

	public void setFDoc(FDoc doc) {
		this.fDoc = doc;
	}
	 */
	
	public String getClaim() {
		return claim;
	}

	public void setClaim(String claim) {
		this.claim = claim;
	}

	public String getGenReceivedNote() {
		return genReceivedNote;
	}

	public void setGenReceivedNote(String genReceivedNote) {
		this.genReceivedNote = genReceivedNote;
	}

	public String getClaimIntervalInfo() {
		return claimIntervalInfo;
	}

	public void setClaimIntervalInfo(String claimIntervalInfo) {
		this.claimIntervalInfo = claimIntervalInfo;
	}

	public String getCreateItem() {
		return createItem;
	}

	public void setCreateItem(String createItem) {
		this.createItem = createItem;
	}

	public String getRecType() {
		return recType;
	}

	public void setRecType(String recType) {
		this.recType = recType;
	}
	
}
