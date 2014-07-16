package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.lu.oleconvert.LU_DBLoadInstances;

@Embeddable
@XmlType(name="callNumber", propOrder={"type", "prefix", "number", "classificationPart", "itemPart", "shelvingScheme", "shelvingOrder"})
public class CallNumber implements Serializable {

	public static List<String> SCprefixes = new ArrayList<String>() {{
		// order is important here, or else SC F will match SC F08 before SC F08 is encountered in loop later on
		add("SC F08");
		add("SC F");
		add("SC N");
		add("SC Office");
		add("SC ORef");
		add("SC O"); 
		add("SC Pp");
		add("SC P");
		add("SC Qq");
		add("SC Q");
		add("SC Ref");
		add("SC R");
		add("SC S");
	}};
	
	public static List<String> specialShelvingLocations  = new ArrayList<String>() {{
		add("L-SPCOLL-A");
		add("L-SPCOLL-B");
		add("L-SPCOLL-C");
		add("L-SPCOLL-D");
		add("L-SPCOLL-E");
		add("L-SPCOLL-F");
		add("L-SPCOLL-G"); 
	}};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3652171135563372983L;

	//private String type;
	private String prefix;
	private String number;
	//private ShelvingScheme shelvingScheme;
	private ShelvingOrder shelvingOrder;
	
	// optional ones, which should be present for the callNumber inside an item, but not the one inside an OLEHoldings
	//private String classificationPart;
	//private String itemPart;
	
	public CallNumber() {
		super();
		//type = "";
		prefix = number = "";
		//shelvingScheme = new ShelvingScheme();
		shelvingOrder = new ShelvingOrder();
		shelvingOrder.setShelvingOrder("X"); // there has to be value here for call number browse to work in OLE
	}
	
	/*
	@XmlElement(name="classificationPart")
	public String getClassificationPart() {
		return classificationPart;
	}

	public void setClassificationPart(String classificationPart) {
		this.classificationPart = classificationPart;
	}

	@XmlElement(name="itemPart")
	public String getItemPart() {
		return itemPart;
	}

	public void setItemPart(String itemPart) {
		this.itemPart = itemPart;
	}
	
	// TODO: I guess we'll need a new class for this one?
	// There's a table OLE_DS_CALL_NUMBER_TYPE_T, which this
	// should be related to
	@XmlElement(name="type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	*/
	
	@Column(name="CALL_NUMBER_PREFIX")
	@XmlElement(name="prefix", required=true, nillable=true)
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Column(name="CALL_NUMBER")
	@XmlElement(name="number")
	public String getNumber() {
		return number;
	}

	public boolean inRightLocation(FlatLocation loc) {
		if ( loc.getLocCodeString().startsWith("LEHIGH/LIND/SPCOLL")) {
			return true;
		} else {
			for ( String shelvingLoc : specialShelvingLocations ) {
				if ( loc.getLocCodeString().contains(shelvingLoc) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
	
	public void setNumber(String number, FlatLocation loc) {
		/* I couldn't figure out what code was still setting the wrong call number, so I put this here to find it
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		System.err.println("Setting call number to " + number);
		for ( StackTraceElement s : stack ) {
			System.err.print(s.toString() + ", ");
		}
		System.err.println();
		*/
		// Save a little time by only looping over the SC prefixes
		// if the number starts what they all have in common
		LU_DBLoadInstances.Log(System.out, "Setting call number, input: " + number + ", location: " + loc.getLocCodeString(), 
				               LU_DBLoadInstances.LOG_DEBUG);
		if ( number.startsWith("SC ") && inRightLocation(loc) ) {
			for ( String prefix : CallNumber.SCprefixes ) {
				if ( number.startsWith(prefix) ) {
					number = number.replaceFirst(prefix, "");
					number = number.trim();
					this.setPrefix(prefix);
					this.number = number;
					return;
				}
			}
		}
		this.number = number;
	}

	/*
	@XmlElement(name="shelvingScheme")	
	public ShelvingScheme getShelvingScheme() {
		return shelvingScheme;
	}

	public void setShelvingScheme(ShelvingScheme shelvingScheme) {
		this.shelvingScheme = shelvingScheme;
	}
	*/
	
	@Embedded
	@XmlElement(name="shelvingOrder")	
	public ShelvingOrder getShelvingOrder() {
		return shelvingOrder;
	}

	public void setShelvingOrder(ShelvingOrder shelvingOrder) {
		this.shelvingOrder = shelvingOrder;
	}	
	
}
