package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="ole_ser_rcv_rec")
public class SerialsReceiving implements Serializable {

	/*
	 * TODO: create attributes for each of these columns 
	 * "PO_ID","PRINT_LBL","PUBLIC_DISPLAY","SER_RCPT_LOC","SER_RCV_REC","SUBSCR_STAT","TREATMENT_INSTR_NOTE","UNBOUND_LOC","URGENT_NOTE","VENDOR","CREATE_DATE","OPTR_ID","MACH_ID","SUBSCR_STAT_DT","OBJ_ID","VER_NBR","ACTIVE"
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6061865930462198074L;

	@Id
	@GeneratedValue
	@Column(name="SER_RCV_REC_ID")
	private Long id;
	
	@Column(name="FDOC_NBR")
	private String fdocNumber;
	
	@Column(name="BIB_ID")
	private String bibId;
	
	// NULL in all the Chicago data, not sure what to do with this
	@Column(name="FDOC_NBR")
	private String fDocNumber;
	
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
	
}
