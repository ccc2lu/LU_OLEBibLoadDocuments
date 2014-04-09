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
@Table(name="ole_ser_rcv_rec_typ_t")
public class SerialsReceivingRecType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8347241766689120339L;

	public SerialsReceivingRecType() {
		super();
	}
	
	@Id
	@GeneratedValue
	@Column(name="SER_RCV_REC_TYP_ID")
	private String id;
	
	/*
	"SER_RCV_REC_TYP_ID","SER_RCV_REC_ID","RCV_REC_TYP","ACTN_DATE","ACTN_INTRVL","CHRON_CAPTN_LVL1","CHRON_CAPTN_LVL2","CHRON_CAPTN_LVL3","CHRON_CAPTN_LVL4","ENUM_CAPTN_LVL1","ENUM_CAPTN_LVL2","ENUM_CAPTN_LVL3","ENUM_CAPTN_LVL4","ENUM_CAPTN_LVL5","ENUM_CAPTN_LVL6","OBJ_ID","VER_NBR"
	"1","140891","Main","","30","","","","","","","","","","","4ad1f7f4-b5c9-4acc-b408-4f1821c8d292","1"
	"10","487066","Main","","15","","","","","","","","","","","6499c659-4d16-459b-904b-c8ed639e8a13","1"
	"100","554905","Main","","90","","","","","","","","","","","a690ef46-c8f2-45d6-a0d0-a3804cf941ec","1"
	*/
	
	@OneToOne
	@JoinColumn(name="SER_RCV_REC_ID")
	private SerialsReceiving serialsReceivingRec;
	
	@Column(name="RCV_REC_TYP")
	private String recType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SerialsReceiving getSerialsReceivingRec() {
		return serialsReceivingRec;
	}

	public void setSerialsReceivingRec(SerialsReceiving serialsReceivingRec) {
		this.serialsReceivingRec = serialsReceivingRec;
	}

	public String getRecType() {
		return recType;
	}

	public void setRecType(String recType) {
		this.recType = recType;
	}
	
	
}
