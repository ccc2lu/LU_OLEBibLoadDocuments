package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.UUID;

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
		this.versionNbr = 1; 
		this.setObjId(UUID.randomUUID().toString());
	}
	
	@Id
	@GeneratedValue
	@Column(name="SER_RCV_REC_TYP_ID")
	private String id;
	
	@OneToOne
	@JoinColumn(name="SER_RCV_REC_ID")
	private SerialsReceiving serialsReceiving;
	
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

	public String getEnumCaptionLvl1() {
		return enumCaptionLvl1;
	}

	public void setEnumCaptionLvl1(String enumCaptionLvl1) {
		this.enumCaptionLvl1 = enumCaptionLvl1;
	}

	public String getEnumCaptionLvl2() {
		return enumCaptionLvl2;
	}

	public void setEnumCaptionLvl2(String enumCaptionLvl2) {
		this.enumCaptionLvl2 = enumCaptionLvl2;
	}

	public String getEnumCaptionLvl3() {
		return enumCaptionLvl3;
	}

	public void setEnumCaptionLvl3(String enumCaptionLvl3) {
		this.enumCaptionLvl3 = enumCaptionLvl3;
	}

	public String getEnumCaptionLvl4() {
		return enumCaptionLvl4;
	}

	public void setEnumCaptionLvl4(String enumCaptionLvl4) {
		this.enumCaptionLvl4 = enumCaptionLvl4;
	}

	public String getEnumCaptionLvl5() {
		return enumCaptionLvl5;
	}

	public void setEnumCaptionLvl5(String enumCaptionLvl5) {
		this.enumCaptionLvl5 = enumCaptionLvl5;
	}

	public String getEnumCaptionLvl6() {
		return enumCaptionLvl6;
	}

	public void setEnumCaptionLvl6(String enumCaptionLvl6) {
		this.enumCaptionLvl6 = enumCaptionLvl6;
	}

	public String getChronCaptionLvl1() {
		return chronCaptionLvl1;
	}

	public void setChronCaptionLvl1(String chronCaptionLvl1) {
		this.chronCaptionLvl1 = chronCaptionLvl1;
	}

	public String getChronCaptionLvl2() {
		return chronCaptionLvl2;
	}

	public void setChronCaptionLvl2(String chronCaptionLvl2) {
		this.chronCaptionLvl2 = chronCaptionLvl2;
	}

	public String getChronCaptionLvl3() {
		return chronCaptionLvl3;
	}

	public void setChronCaptionLvl3(String chronCaptionLvl3) {
		this.chronCaptionLvl3 = chronCaptionLvl3;
	}

	public String getChronCaptionLvl4() {
		return chronCaptionLvl4;
	}

	public void setChronCaptionLvl4(String chronCaptionLvl4) {
		this.chronCaptionLvl4 = chronCaptionLvl4;
	}

	public String getActionDate() {
		return actionDate;
	}

	public void setActionDate(String actionDate) {
		this.actionDate = actionDate;
	}

	public String getActionInterval() {
		return actionInterval;
	}

	public void setActionInterval(String actionInterval) {
		this.actionInterval = actionInterval;
	}

	@Column(name="RCV_REC_TYP")
	private String recType;

	// All 1 in Chicago data
	@Column(name="VER_NBR")
	private int versionNbr;
	
	// Set to a UUID in Chicago data
	@Column(name="OBJ_ID")
	private String objId;
	
	// NULL in Chicago's CSV data, note in mapping file saying:
	// from enum_pattern_level.label use serial# and ord={0..5}
	@Column(name="ENUM_CAPTN_LVL1")
	private String enumCaptionLvl1;
	@Column(name="ENUM_CAPTN_LVL2")
	private String enumCaptionLvl2;
	@Column(name="ENUM_CAPTN_LVL3")
	private String enumCaptionLvl3;
	@Column(name="ENUM_CAPTN_LVL4")
	private String enumCaptionLvl4;
	@Column(name="ENUM_CAPTN_LVL5")
	private String enumCaptionLvl5;
	@Column(name="ENUM_CAPTN_LVL6")
	private String enumCaptionLvl6;
	
	// All NULL in Chicago data, with a note saying: 
	// "use fm_ole_sercaptions to get chron_pattern from olga, then Julie Stauffer will provide mapping to OLE patterns"
	@Column(name="CHRON_CAPTN_LVL1")
	private String chronCaptionLvl1;
	@Column(name="CHRON_CAPTN_LVL2")
	private String chronCaptionLvl2;
	@Column(name="CHRON_CAPTN_LVL3")
	private String chronCaptionLvl3;
	@Column(name="CHRON_CAPTN_LVL4")
	private String chronCaptionLvl4;
	
	// NULL in Chicago data
	@Column(name="ACTN_DATE")
	private String actionDate;
	
	// Set to "first claim delay" in Chicago data
	@Column(name="ACTN_INTRVL")
	private String actionInterval;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SerialsReceiving getSerialsReceiving() {
		return serialsReceiving;
	}

	public void setSerialsReceiving(SerialsReceiving serialsReceivingRec) {
		this.serialsReceiving = serialsReceivingRec;
	}

	public String getRecType() {
		return recType;
	}

	public void setRecType(String recType) {
		this.recType = recType;
	}
	
	
}
