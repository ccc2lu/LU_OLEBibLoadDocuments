package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="sourceHoldings")
public class SourceHoldings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8980510462149213681L;
	private String primary;
	
	public SourceHoldings() {
		super();
	}

	@XmlAttribute(name="primary")
	public String getPrimary() {
		return primary;
	}

	public void setPrimary(String primary) {
		this.primary = primary;
	}
	
	

}
