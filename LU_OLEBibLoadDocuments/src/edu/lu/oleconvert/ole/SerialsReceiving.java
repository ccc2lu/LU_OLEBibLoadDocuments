package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ole_ser_rcv_rec")
public class SerialsReceiving implements Serializable {

	/*
	 * 
	 * "SER_RCV_REC_ID","FDOC_NBR","BIB_ID","RCV_REC_TYP","CLAIM","CLAIM_INTRVL_INFO","CREATE_ITEM","GEN_RCV_NOTE","INSTANCE_ID","PO_ID","PRINT_LBL","PUBLIC_DISPLAY","SER_RCPT_LOC","SER_RCV_REC","SUBSCR_STAT","TREATMENT_INSTR_NOTE","UNBOUND_LOC","URGENT_NOTE","VENDOR","CREATE_DATE","OPTR_ID","MACH_ID","SUBSCR_STAT_DT","OBJ_ID","VER_NBR","ACTIVE"
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6061865930462198074L;

	@Id
	@GeneratedValue
	@Column(name="SER_RCV_REC_ID")
	private Long id;
	
	
	@Column(name="BIB_ID")
	public String bibId;
	public SerialsReceiving() {
		super();
	}
}
