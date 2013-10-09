package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="instance", propOrder={"instanceIdentifier", "resourceIdentifier", "oleHoldings", "sourceHoldings", "items"})
public class Instance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4883146897656280004L;

	private String instanceIdentifier;
	private String resourceIdentifier;
	private OLEHoldings oleHoldings; 
	private SourceHoldings sourceHoldings;
	private Items items;
	
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
	
	@XmlElement(name="resourceIdentitier")
	public String getResourceIdentifier() {
		return resourceIdentifier;
	}
	public void setResourceIdentifier(String resourceIdentifier) {
		this.resourceIdentifier = resourceIdentifier;
	}
	
}
