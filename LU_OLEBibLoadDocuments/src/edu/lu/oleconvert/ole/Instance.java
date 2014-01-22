package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@XmlType(name="instance", propOrder={"instanceIdentifier", "resourceIdentifier", "formerResourceIdentifiers", "oleHoldings", "sourceHoldings", "items"})
public class Instance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4883146897656280004L;

	private String instanceIdentifier;
	private String resourceIdentifier;
	private ArrayList<FormerIdentifier> formerResourceIdentifiers;
	private OLEHoldings oleHoldings; 
	private SourceHoldings sourceHoldings;
	private Items items;
	
	@XmlElement(name="formerResourceIdentifier")
	public ArrayList<FormerIdentifier> getFormerResourceIdentifiers() {
		return formerResourceIdentifiers;
	}

	public void setFormerResourceIdentifiers(ArrayList<FormerIdentifier> formerResourceIdentifiers) {
		this.formerResourceIdentifiers = formerResourceIdentifiers;
	}
	
	@XmlElement(name="oleHoldings")
	public OLEHoldings getOleHoldings() {
		return oleHoldings;
	}
	
	public void setOleHoldings(OLEHoldings oleHoldings) {
		this.oleHoldings = oleHoldings;
	}
	
	@XmlElement(name="sourceHoldings")
	public SourceHoldings getSourceHoldings() {
		return sourceHoldings;
	}
	
	public void setSourceHoldings(SourceHoldings sourceHoldings) {
		this.sourceHoldings = sourceHoldings;
	}
	
	@XmlElement(name="items")
	public Items getItems() {
		return items;
	}
	
	public void setItems(Items items) {
		this.items = items;
	}
	
	@XmlElement(name="instanceIdentifier")
	public String getInstanceIdentifier() {
		return instanceIdentifier;
	}
	public void setInstanceIdentifier(String instanceIdentifier) {
		this.instanceIdentifier = instanceIdentifier;
	}
	
	@XmlElement(name="resourceIdentifier")
	public String getResourceIdentifier() {
		return resourceIdentifier;
	}
	public void setResourceIdentifier(String resourceIdentifier) {
		this.resourceIdentifier = resourceIdentifier;
	}
	
}
