package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
//@Table(name="ole_ds_ext_ownership_type_t")
@Table(name="ole_cat_rcpt_stat_t")
public class ReceiptStatus implements Serializable {


	private Long id;
	private String code;
	private String name;
	private String source;
	private String sourceDate;
	private String obj_id;
	private String active;
	private int ver_nbr;
	

	public ReceiptStatus() {
		super();
		ver_nbr = 1;
		active = "Y";
		this.setObj_id(UUID.randomUUID().toString());
		this.setSource("LU SIRSI");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String datestr = df.format(Calendar.getInstance().getTime());
		this.setSourceDate(datestr);
	}

	public ReceiptStatus(String code, String name) {
		this();
		this.setCode(code);
		this.setName(name);
	}
	
	@Id
	@GeneratedValue
	//@Column(name="EXT_OWNERSHIP_TYPE_ID")
	@Column(name="RCPT_STAT_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	//@Column(name="CODE")
	@Column(name="RCPT_STAT_CD")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	//@Column(name="NAME")
	@Column(name="RCPT_STAT_NM")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name="SRC")
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Column(name="SRC_DT")
	public String getSourceDate() {
		return sourceDate;
	}

	public void setSourceDate(String sourceDate) {
		this.sourceDate = sourceDate;
	}

	@Column(name="ROW_ACT_IND")
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}

	@Column(name="OBJ_ID")
	public String getObj_id() {
		return obj_id;
	}
	public void setObj_id(String obj_id) {
		this.obj_id = obj_id;
	}

	@Column(name="VER_NBR")
	public int getVer_nbr() {
		return ver_nbr;
	}
	public void setVer_nbr(int ver_nbr) {
		this.ver_nbr = ver_nbr;
	}
	
}

/*
@Entity
@Table(name="ole_ds_receipt_status_t")
public class ReceiptStatus implements Serializable {

	private Long id;
	private String code;
	private String name;
	
	public ReceiptStatus() {
		super();
	}
	public ReceiptStatus(String code, String name) {
		this();
		this.setCode(code);
		this.setName(name);
	}
	
	@Id
	@GeneratedValue
	@Column(name="RECEIPT_STATUS_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="CODE")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name="NAME")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
*/
