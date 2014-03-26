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
@Table(name="ole_dlvr_item_avail_stat_t")
public class Deliver_ItemStatus implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8848039911812066301L;
	
	private Long id;
	private String code;
	private String name;
	private String active;
	private String obj_id;
	private int ver_nbr;
	
	public Deliver_ItemStatus() {
		super();
		ver_nbr = 1;
		active = "Y";
		this.setObj_id(UUID.randomUUID().toString());
	}

	public Deliver_ItemStatus(String code, String name) {
		this();
		this.code = code;
		this.name = name;
	}

	@Id
	@GeneratedValue
	@Column(name="ITEM_AVAIL_STAT_ID")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="ITEM_AVAIL_STAT_CD")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	@Column(name="ITEM_AVAIL_STAT_NM")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
