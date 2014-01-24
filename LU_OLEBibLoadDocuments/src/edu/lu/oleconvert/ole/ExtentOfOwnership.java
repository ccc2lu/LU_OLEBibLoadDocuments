package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name="ole_ds_ext_ownership_t")
@XmlType(name="extentOfOwnership", propOrder={"textualHoldings", "type", "notes"})
public class ExtentOfOwnership implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9159363905445454975L;

	private String textualHoldings;
	private ExtentOfOwnershipType type;
	private ArrayList<ExtentOfOwnershipNote> notes;
	private OLEHoldings oleHoldings;
	
	public ExtentOfOwnership() {
		super();
		notes = new ArrayList<ExtentOfOwnershipNote>();
	}

	@ManyToOne
	@JoinColumn(name="HOLDINGS_ID")
	public OLEHoldings getOLEHoldings() {
		return this.oleHoldings;
	}
	public void setOLEHoldings(OLEHoldings holdings) {
		this.oleHoldings = holdings;
	}
	
	@Column(name="TEXT")
	@XmlElement(name="textualHoldings")
	public String getTextualHoldings() {
		return textualHoldings;
	}

	public void setTextualHoldings(String textualHoldings) {
		this.textualHoldings = textualHoldings;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="EXT_OWNERSHIP_TYPE_ID")
	@XmlElement(name="type")
	public ExtentOfOwnershipType getType() {
		return type;
	}

	public void setType(ExtentOfOwnershipType type) {
		this.type = type;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="extentofownership")
	@XmlElement(name="note")
	public ArrayList<ExtentOfOwnershipNote> getNotes() {
		return notes;
	}

	public void setNotes(ArrayList<ExtentOfOwnershipNote> notes) {
		this.notes = notes;
	}

	
}


