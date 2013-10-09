package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="highDensityStorage", propOrder={"row", "module", "shelf", "tray"})
public class HighDensityStorage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -632136852839909826L;
	
	private String row;
	private String module;
	private String shelf;
	private String tray;
	
	public HighDensityStorage() {
		super();
		row = module = shelf = tray = "";
	}

	@XmlElement(name="row", required=true, nillable=true)
	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

	@XmlElement(name="module", required=true, nillable=true)	
	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	@XmlElement(name="shelf", required=true, nillable=true)	
	public String getShelf() {
		return shelf;
	}

	public void setShelf(String shelf) {
		this.shelf = shelf;
	}

	@XmlElement(name="tray", required=true, nillable=true)	
	public String getTray() {
		return tray;
	}

	public void setTray(String tray) {
		this.tray = tray;
	}
	
}
