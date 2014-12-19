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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.mysql.jdbc.Clob;

import edu.lu.oleconvert.LU_BuildOLELoadDocs;
import edu.lu.oleconvert.LU_DBLoadInstances;
import edu.lu.oleconvert.ole.Bib;

@Entity
@Table(name="bib_leader_t")
public class Leader implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6380330738651781920L;
	
	@OneToOne
	@JoinColumn(name="BIB_ID")
	Bib bib;
	
	@Id
	@GeneratedValue
	@Column(name="leader_id")
	int id;
	
	@Column(name="reclength")
	int recordLength;
	
	@Column(name="recstatus")
	char recStatus;
	
	@Column(name="rectype")
	char recType;
	
	@Column(name="biblevel")
	char bibLevel;
	
	@Column(name="controltype")
	char controlType;
	
	@Column(name="charcoding")
	char charCoding;
	
	@Column(name="indcount")
	int indcount;
	
	@Column(name="codecount")
	int codecount;
	
	@Column(name="baseaddr")
	int baseAddr;
	
	@Column(name="encodinglevel")
	char encodingLevel;
	
	@Column(name="catform")
	char catalogingForm;
	
	@Column(name="multresreclevel")
	char multipartResRecLevel;
	
	public Leader() {
		super();
	}
	
	// we don't need the length-of-field length, length of starting character position, or length of implementation-defined parts
	public Leader(Bib b, org.marc4j.marc.Leader leader) {
		this();
		String leaderStr = leader.toString();
		this.setBib(b);
		this.setRecordLength(leader.getRecordLength());
		this.setRecStatus(leader.getRecordStatus());
		this.setRecType(leader.getTypeOfRecord());
		this.setBibLevel(leaderStr.charAt(7));
		this.setControlType(leaderStr.charAt(8));
		this.setCharCoding(leaderStr.charAt(9));
		this.setIndcount(leaderStr.charAt(10) - '0');
		this.setCodecount(leaderStr.charAt(11) - '0');
		this.setBaseAddr(leader.getBaseAddressOfData());
		this.setEncodingLevel(leaderStr.charAt(17));
		this.setCatalogingForm(leaderStr.charAt(18));
		this.setMultipartResRecLevel(leaderStr.charAt(19));
	}
	
	public Bib getBib() {
		return bib;
	}

	public void setBib(Bib bib) {
		this.bib = bib;
	}

	public int getRecordLength() {
		return recordLength;
	}

	public void setRecordLength(int i) {
		this.recordLength = i;
	}

	public char getRecStatus() {
		return recStatus;
	}

	public void setRecStatus(char recStatus) {
		this.recStatus = recStatus;
	}

	public char getRecType() {
		return recType;
	}

	public void setRecType(char type) {
		this.recType = type;
	}

	public char getBibLevel() {
		return bibLevel;
	}

	public void setBibLevel(char bibLevel) {
		this.bibLevel = bibLevel;
	}

	public char getControlType() {
		return controlType;
	}

	public void setControlType(char controlType) {
		this.controlType = controlType;
	}

	public char getCharCoding() {
		return charCoding;
	}

	public void setCharCoding(char charCoding) {
		this.charCoding = charCoding;
	}

	public int getIndcount() {
		return indcount;
	}

	public void setIndcount(int indcount) {
		this.indcount = indcount;
	}

	public int getCodecount() {
		return codecount;
	}

	public void setCodecount(int codecount) {
		this.codecount = codecount;
	}

	public int getBaseAddr() {
		return baseAddr;
	}

	public void setBaseAddr(int baseAddr) {
		this.baseAddr = baseAddr;
	}

	public char getEncodingLevel() {
		return encodingLevel;
	}

	public void setEncodingLevel(char encodingLevel) {
		this.encodingLevel = encodingLevel;
	}

	public char getCatalogingForm() {
		return catalogingForm;
	}

	public void setCatalogingForm(char catalogingForm) {
		this.catalogingForm = catalogingForm;
	}

	public char getMultipartResRecLevel() {
		return multipartResRecLevel;
	}

	public void setMultipartResRecLevel(char multipartResRecLevel) {
		this.multipartResRecLevel = multipartResRecLevel;
	}
	
}
