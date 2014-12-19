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
@Table(name="bib_controlfield_t")
public class ControlField implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5269329785285706129L;

	@ManyToOne
	@JoinColumn(name="BIB_ID")
	Bib bib;
	
	@Id
	@GeneratedValue
	@Column(name="cfield_id")
	int id;

	@Column(name="tag")
	String tag;
	
	@Column(name="value")
	String value;
	
	public ControlField() {
		super();
	}

	public Bib getBib() {
		return bib;
	}

	public void setBib(Bib bib) {
		this.bib = bib;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
