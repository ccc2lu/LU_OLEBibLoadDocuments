package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.lu.oleconvert.LU_DBLoadInstances;

@Entity
@Table(name="ole_ds_ext_ownership_t")
@XmlType(name="extentOfOwnership", propOrder={"textualHoldings", "type", "notes"})
public class ExtentOfOwnership implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9159363905445454975L;

	private Long id;
	private String textualHoldings;
	private ExtentOfOwnershipType type;
	private List<ExtentOfOwnershipNote> notes;
	private OLEHoldings oleHoldings;
	
	public ExtentOfOwnership() {
		super();
		notes = new ArrayList<ExtentOfOwnershipNote>();
	}

	@Id
	@GeneratedValue
	@Column(name="EXT_OWNERSHIP_ID")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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

	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="EXT_OWNERSHIP_TYPE_ID")
	@XmlElement(name="type")
	public ExtentOfOwnershipType getType() {
		return type;
	}

	public void setType(ExtentOfOwnershipType type) {
		this.type = type;
	}
	public void setType(String code, String name) {
		ExtentOfOwnershipType type;
		TypedQuery<ExtentOfOwnershipType> query = LU_DBLoadInstances.em.createQuery("SELECT t FROM ExtentOfOwnershipType t WHERE t.code='" + code + "'", ExtentOfOwnershipType.class);
		query.setHint("org.hibernate.cacheable", true);
		List<ExtentOfOwnershipType> results = query.getResultList();
		if ( results.size() == 0 ) {
			type = new ExtentOfOwnershipType();
			type.setCode(code);
			type.setName(name);
		} else {
			type = results.get(0);
		}		
		this.setType(type);
	}
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="extentOfOwnership", cascade=CascadeType.ALL)
	@XmlElement(name="note")
	public List<ExtentOfOwnershipNote> getNotes() {
		return notes;
	}

	public void setNotes(List<ExtentOfOwnershipNote> notes) {
		this.notes = notes;
	}

	
}


