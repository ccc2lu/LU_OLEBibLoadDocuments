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
@Table(name="bib_datafield_t")
public class DataField implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4856439055553409427L;

	@ManyToOne
	@JoinColumn(name="BIB_ID")
	Bib bib;
	
	@Id
	@GeneratedValue
	@Column(name="dfield_id")
	int id;

	@Column(name="tag")
	String tag;
	
	@Column(name="ind1")
	String ind1;
	
	@Column(name="ind2")
	String ind2;

	public DataField() {
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

	public String getInd1() {
		return ind1;
	}

	public void setInd1(String ind1) {
		this.ind1 = ind1;
	}

	public String getInd2() {
		return ind2;
	}

	public void setInd2(String ind2) {
		this.ind2 = ind2;
	}


	
	
}
