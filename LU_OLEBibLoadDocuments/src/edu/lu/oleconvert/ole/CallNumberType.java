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
//@Table(name="ole_ds_call_number_type_m_t")
//@Table(name="ole_ds_call_number_type_t")
@Table(name="ole_cat_shvlg_schm_t")
public class CallNumberType implements Serializable {

	private Long id;
	private String code;
	private String name;
	private String src;
	private String src_date;
	private String active;
	private String obj_id;
	private int ver_nbr;
	
	public CallNumberType() {
		super();
		ver_nbr = 1;
		src = "LU SIRSI";
		active = "Y";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String datestr = df.format(Calendar.getInstance().getTime());
		this.setSrcDate(datestr);
		this.setObj_id(UUID.randomUUID().toString());
	}

	public CallNumberType(String code, String name) {
		this();
		this.setCode(code);
		this.setName(name);
	}
	
	@Id
	@GeneratedValue
	//@Column(name="CALL_NUMBER_TYPE_ID")
	@Column(name="SHVLG_SCHM_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="SHVLG_SCHM_CD")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name="SHVLG_SCHM_NM")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name="SRC")
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}

	@Column(name="SRC_DT")
	public String getSrcDate() {
		return this.src_date;
	}
	public void setSrcDate(String date) {
		this.src_date = date;
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
