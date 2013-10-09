package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="extentOfOwnership", propOrder={"textualHoldings", "type", "notes"})
public class ExtentOfOwnership implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9159363905445454975L;

	private String textualHoldings;
	private String type;
	private ArrayList<Note> notes;
	
	public ExtentOfOwnership() {
		super();
		notes = new ArrayList<Note>();
	}

	@XmlElement(name="textualHoldings")
	public String getTextualHoldings() {
		return textualHoldings;
	}

	public void setTextualHoldings(String textualHoldings) {
		this.textualHoldings = textualHoldings;
	}

	@XmlElement(name="type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name="note")
	public ArrayList<Note> getNotes() {
		return notes;
	}

	public void setNotes(ArrayList<Note> notes) {
		this.notes = notes;
	}

	
}


