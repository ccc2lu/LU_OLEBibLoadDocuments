package edu.lu.LoadOLEReportingDB;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.mysql.jdbc.Clob;

import edu.lu.oleconvert.LU_BuildOLELoadDocs;
import edu.lu.oleconvert.LU_DBLoadInstances;
import edu.lu.oleconvert.ole.Bib;

@Entity
@Table(name="bib_subfield_t")
public class SubField implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8181442686363635132L;

	@ManyToOne
	@JoinColumn(name="DFIELD_ID")
	DataField dfield;
	
	@Id
	@GeneratedValue
	@Column(name="subfield_id")
	int id;

	@Column(name="code")
	String code;
	
	@Column(name="value")
	String value;
	
	public SubField() {
		super();
	}

	public DataField getDfield() {
		return dfield;
	}

	public void setDfield(DataField dfield) {
		this.dfield = dfield;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


}
