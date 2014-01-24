package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name="ole_ds_high_density_storage_t")
@XmlType(name="highDensityStorage", propOrder={"row", "module", "shelf", "tray"})
public class HighDensityStorage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -632136852839909826L;
	
	private Long id;
	private String row;
	private String module;
	private String shelf;
	private String tray;
	
	public HighDensityStorage() {
		super();
		row = module = shelf = tray = "";
	}

	@Id
	@GeneratedValue
	@Column(name="HIGH_DENSITY_STORAGE_ID")
	public Long getId() {
		return this.id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name="HIGH_DENSITY_ROW")
	@XmlElement(name="row", required=true, nillable=true)
	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

	@Column(name="HIGH_DENSITY_MODULE")
	@XmlElement(name="module", required=true, nillable=true)	
	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	@Column(name="HIGH_DENSITY_SHELF")
	@XmlElement(name="shelf", required=true, nillable=true)	
	public String getShelf() {
		return shelf;
	}

	public void setShelf(String shelf) {
		this.shelf = shelf;
	}

	@Column(name="HIGH_DENSITY_TRAY")
	@XmlElement(name="tray", required=true, nillable=true)	
	public String getTray() {
		return tray;
	}

	public void setTray(String tray) {
		this.tray = tray;
	}
	
}
