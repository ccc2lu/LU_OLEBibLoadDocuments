package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.time.DateUtils;

import edu.lu.oleconvert.LU_DBLoadInstances;

@Entity
@Table(name="ole_ser_rcv_his_rec")
public class SerialsReceivingHisRec implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6033986343219189671L;
	
	public SerialsReceivingHisRec() {
		super();
		this.versionNbr = 1; 
		this.setObjId(UUID.randomUUID().toString());
	}
	
	public SerialsReceivingHisRec(SerialsReceiving sr) {
		this();
		this.setSerialsReceiving(sr);
	}
	
	@ManyToOne
	@JoinColumn(name="SER_RCV_REC_ID")
	private SerialsReceiving serialsReceiving;
	
	@Id
	@GeneratedValue
	@Column(name="SER_RCPT_HIS_REC_ID")
	private Long id;

	/*
	 * "RCPT_DATE","PUB_RCPT","STAFF_ONLY_RCPT"
	 * 
	 */
	
	// All NULL in Chicago data, with a note saying: 
	// "use fm_ole_sercaptions to get chron_pattern from olga, then Julie Stauffer will provide mapping to OLE patterns"
	@Column(name="CHRON_LVL_1")
	private String chronLvl1;
	@Column(name="CHRON_LVL_2")
	private String chronLvl2;
	@Column(name="CHRON_LVL_3")
	private String chronLvl3;
	
	@Column(name="PUB_DISPLAY")
	private String publicDisplay;
	
	@Column(name="SER_RCPT_NOTE")
	private String receiptNote;
	
	@Column(name="OPTR_ID")
	private String operatorId;
	
	@Column(name="MACH_ID")
	private String machineId;
	
	// All set to Received in Chicago data
	@Column(name="RCPT_STAT")
	private String receiptStat;
	
	// All blank in Chicago data
	@Column(name="RCPT_DATE")
	private String receiptDate;
	
	// All blank in Chicago data
	@Column(name="PUB_RCPT")
	private String publicReceipt;
	
	// All blank in Chicago data
	@Column(name="STAFF_ONLY_RCPT")
	private String staffOnlyReceipt;
	
	// In chicago data, volume and issue both in this field, e g. "v. 55 no. 4"
	@Column(name="ENUM_LVL_1")
	private String enumLvl1;
	@Column(name="ENUM_LVL_2")
	private String enumLvl2;
	@Column(name="ENUM_LVL_3")
	private String enumLvl3;
	@Column(name="ENUM_LVL_4")
	private String enumLvl4;
	@Column(name="ENUM_LVL_5")
	private String enumLvl5;
	@Column(name="ENUM_LVL_6")
	private String enumLvl6;
	
	@Column(name="CLAIM_COUNT")
	private String claimCount;
	
	@Column(name="CLAIM_DATE")
	private String claimDate;
	
	@Column(name="CLAIM_TYPE")
	private String claimType;
	
	@Column(name="CLAIM_RESP")
	private String claimResponse;
	
	@Column(name="RCV_REC_TYP")
	private String recType;

	// All 1 in Chicago data
	@Column(name="VER_NBR")
	private int versionNbr;
	
	// Left blank in Chicago data
	@Column(name="OBJ_ID")
	private String objId;
	
	public SerialsReceiving getSerialsReceiving() {
		return serialsReceiving;
	}

	public void setSerialsReceiving(SerialsReceiving serialsReceiving) {
		this.serialsReceiving = serialsReceiving;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getChronLvl1() {
		return chronLvl1;
	}

	public void setChronLvl1(String chronLvl1) {
		this.chronLvl1 = chronLvl1;
	}

	public String getChronLvl2() {
		return chronLvl2;
	}

	public void setChronLvl2(String chronLvl2) {
		this.chronLvl2 = chronLvl2;
	}

	public String getChronLvl3() {
		return chronLvl3;
	}

	public void setChronLvl3(String chronLvl3) {
		this.chronLvl3 = chronLvl3;
	}

	public String getPublicDisplay() {
		return publicDisplay;
	}

	public void setPublicDisplay(String publicDisplay) {
		this.publicDisplay = publicDisplay;
	}

	public String getReceiptNote() {
		return receiptNote;
	}

	public void setReceiptNote(String receiptNote) {
		this.receiptNote = receiptNote;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	public String getReceiptStat() {
		return receiptStat;
	}

	public void setReceiptStat(String receiptStat) {
		this.receiptStat = receiptStat;
	}

	public String getReceiptDate() {
		return receiptDate;
	}

	public void setReceiptDate(String receiptDate) {
		this.receiptDate = receiptDate;
	}

	public String getPublicReceipt() {
		return publicReceipt;
	}

	public void setPublicReceipt(String publicReceipt) {
		this.publicReceipt = publicReceipt;
	}

	public String getStaffOnlyReceipt() {
		return staffOnlyReceipt;
	}

	public void setStaffOnlyReceipt(String staffOnlyReceipt) {
		this.staffOnlyReceipt = staffOnlyReceipt;
	}

	public String getEnumLvl1() {
		return enumLvl1;
	}

	public void setEnumLvl1(String enumLvl1) {
		this.enumLvl1 = enumLvl1;
	}

	public String getEnumLvl2() {
		return enumLvl2;
	}

	public void setEnumLvl2(String enumLvl2) {
		this.enumLvl2 = enumLvl2;
	}

	public String getEnumLvl3() {
		return enumLvl3;
	}

	public void setEnumLvl3(String enumLvl3) {
		this.enumLvl3 = enumLvl3;
	}

	public String getEnumLvl4() {
		return enumLvl4;
	}

	public void setEnumLvl4(String enumLvl4) {
		this.enumLvl4 = enumLvl4;
	}

	public String getEnumLvl5() {
		return enumLvl5;
	}

	public void setEnumLvl5(String enumLvl5) {
		this.enumLvl5 = enumLvl5;
	}

	public String getEnumLvl6() {
		return enumLvl6;
	}

	public void setEnumLvl6(String enumLvl6) {
		this.enumLvl6 = enumLvl6;
	}

	public String getClaimCount() {
		return claimCount;
	}

	public void setClaimCount(String claimCount) {
		this.claimCount = claimCount;
	}

	public String getClaimDate() {
		return claimDate;
	}

	public void setClaimDate(String claimDate) {
		this.claimDate = claimDate;
	}

	public String getClaimType() {
		return claimType;
	}

	public void setClaimType(String claimType) {
		this.claimType = claimType;
	}

	public String getClaimResponse() {
		return claimResponse;
	}

	public void setClaimResponse(String claimResponse) {
		this.claimResponse = claimResponse;
	}

	public String getRecType() {
		return recType;
	}

	public void setRecType(String recType) {
		this.recType = recType;
	}

	public int getVersionNbr() {
		return versionNbr;
	}

	public void setVersionNbr(int versionNbr) {
		this.versionNbr = versionNbr;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}
	
}
