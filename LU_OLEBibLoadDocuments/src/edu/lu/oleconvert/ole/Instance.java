package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.lu.oleconvert.LU_DBLoadInstances;

//@Entity
//@Table(name="ole_ds_instance_t")
@XmlType(name="instance", propOrder={"instanceIdentifier", "resourceIdentifier", "formerResourceIdentifiers", "oleHoldings", "sourceHoldings", "items"})
public class Instance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4883146897656280004L;
	private static Long instanceNum = (long) 1;

	private Long instanceIdentifier;
	private String resourceIdentifier;
	//private ArrayList<FormerIdentifier> formerResourceIdentifiers;
	private OLEHoldings oleHoldings; 
	
	//private SourceHoldings sourceHoldings;
	//private Items items;
	private List<Item> items;
	
	/*
	@Embedded
	@XmlElement(name="formerResourceIdentifier")
	public ArrayList<FormerIdentifier> getFormerResourceIdentifiers() {
		return formerResourceIdentifiers;
	}

	public void setFormerResourceIdentifiers(ArrayList<FormerIdentifier> formerResourceIdentifiers) {
		this.formerResourceIdentifiers = formerResourceIdentifiers;
	}
	*/
	
	public Instance() {
		// shouldn't be necessary -- happens in the database now
		//this.setInstanceIdentifier(instanceNum++);
		oleHoldings = new OLEHoldings();
		items = new ArrayList<Item>();
	}
	
	public Instance(String bib_id) {
		//this.setInstanceIdentifier(instanceNum++);
		this.setResourceIdentifier(bib_id);
		oleHoldings = new OLEHoldings();
		items = new ArrayList<Item>();
	}
	
	//@OneToOne(fetch=FetchType.LAZY, mappedBy="instance", cascade=CascadeType.ALL)
	//@OneToOne(fetch=FetchType.LAZY, mappedBy="instance")
	@XmlElement(name="oleHoldings")
	public OLEHoldings getOleHoldings() {
		return oleHoldings;
	}
	
	public void setOleHoldings(OLEHoldings oleHoldings) {
		this.oleHoldings = oleHoldings;
	}
	
	/*
	@Embedded
	@XmlElement(name="sourceHoldings")
	public SourceHoldings getSourceHoldings() {
		return sourceHoldings;
	}
	
	public void setSourceHoldings(SourceHoldings sourceHoldings) {
		this.sourceHoldings = sourceHoldings;
	}
	*/
	
	/*
	@XmlElement(name="items")
	public Items getItems() {
		return items;
	}
	
	public void setItems(Items items) {
		this.items = items;
	}
	*/
	
	//@Id
	//@GeneratedValue
	//@Column(name="INSTANCE_ID")
	@XmlElement(name="instanceIdentifier")
	public Long getInstanceIdentifier() {
		return instanceIdentifier;
	}
	public void setInstanceIdentifier(Long instanceIdentifier) {
		this.instanceIdentifier = instanceIdentifier;
	}
	
	//@Column(name="BIB_ID")
	@XmlElement(name="resourceIdentifier")
	public String getResourceIdentifier() {
		return resourceIdentifier;
	}
	public void setResourceIdentifier(String resourceIdentifier) {
		this.resourceIdentifier = resourceIdentifier;
	}
	
}
