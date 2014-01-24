package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name="ole_ds_instance_t")
@XmlType(name="instance", propOrder={"instanceIdentifier", "resourceIdentifier", "formerResourceIdentifiers", "oleHoldings", "sourceHoldings", "items"})
public class Instance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4883146897656280004L;

	private String instanceIdentifier;
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
		oleHoldings = new OLEHoldings();
		items = new ArrayList<Item>();
	}
	
	@OneToOne(fetch=FetchType.LAZY, mappedBy="instance", cascade=CascadeType.ALL)
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
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="instance", cascade=CascadeType.ALL)
	public List<Item> getItems() {
		return this.items;
	}
	public void setItems(List<Item> i) {
		this.items = i;
	}
	
	@Id
	@Column(name="INSTANCE_ID")
	@XmlElement(name="instanceIdentifier")
	public String getInstanceIdentifier() {
		return instanceIdentifier;
	}
	public void setInstanceIdentifier(String instanceIdentifier) {
		this.instanceIdentifier = instanceIdentifier;
	}
	
	@Column(name="BIB_ID")
	@XmlElement(name="resourceIdentifier")
	public String getResourceIdentifier() {
		return resourceIdentifier;
	}
	public void setResourceIdentifier(String resourceIdentifier) {
		this.resourceIdentifier = resourceIdentifier;
	}
	
}
