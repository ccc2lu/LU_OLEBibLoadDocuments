package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
@XmlType(name="locationLevel")
public class LocationLevel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6747313166383932343L;
	
	// Unused at this point
}
