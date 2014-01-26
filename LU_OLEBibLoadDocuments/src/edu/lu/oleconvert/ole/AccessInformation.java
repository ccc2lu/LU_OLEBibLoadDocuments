package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
@XmlType(name="accessInformation")
public class AccessInformation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1570800658590357905L;
	private String barcode;
	private URI uri;
	
	public AccessInformation() {
		super();
		this.barcode = "";
		this.uri = new URI();
		// Need URI to show up as empty open and closing tags at least, for AccessInformation
		this.uri.setUri("");
	}

	@Column(name="BARCODE")
	@XmlElement(name="barcode")
	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	@Embedded
	@XmlElement(name="uri")
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	

}
