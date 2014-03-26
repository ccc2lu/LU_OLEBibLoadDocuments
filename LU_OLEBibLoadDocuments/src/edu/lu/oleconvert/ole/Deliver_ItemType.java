package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

@Entity
@Table(name="ole_cat_itm_typ_t")
public class Deliver_ItemType implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7080374142614088997L;
	
	private Long id;
	private String code;
	private String name;
	private String desc;
	private String src;
	private String src_date;
	private String active;
	private String obj_id;
	private int ver_nbr;
	
	public Deliver_ItemType() {
		super();
		ver_nbr = 1;
		src = "LU SIRSI";
		active = "Y";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String datestr = df.format(Calendar.getInstance().getTime());
		this.setSrcDate(datestr);
		this.setObj_id(UUID.randomUUID().toString());
	}

	public Deliver_ItemType(String code, String name) {
		this();
		this.code = code;
		this.name = name;
		this.desc = name;
	}

	public Deliver_ItemType(String code, String name, String desc) {
		this();
		this.code = code;
		this.name = name;
		this.desc = desc;
	}

	@Id
	@GeneratedValue
	@Column(name="ITM_TYP_CD_ID")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="ITM_TYP_CD")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	@Column(name="ITM_TYP_NM")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Column(name="ITM_TYP_DESC")
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
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
