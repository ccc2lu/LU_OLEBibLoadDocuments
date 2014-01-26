package edu.lu.oleconvert.ole;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//@XmlType(name="itemType", propOrder={"codeValue", "fullValue", "typeOrSource"})
@Entity
@Table(name="ole_ds_item_type_t")
@XmlType(name="itemType", propOrder={"fullValue", "typeOrSource"})
public class ItemType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1290250942532107793L;

	private Long id;
	private String code;
	private String name;
	
	public ItemType() {
		super();		
	}

	@Id
	@GeneratedValue
	@Column(name="ITEM_TYPE_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="CODE")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name="NAME")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	// Old code
	// codeValue is in the .xsd for itemType, but not in the sample ingest file
	// The ingest process seems to choke on it with a 
	//protected String codeValue;
	/*
	private String fullValue;
	private TypeOrSource typeOrSource;
	
	public ItemType() {
		super();
		fullValue = "";
		typeOrSource = new TypeOrSource();
	}
*/
	/*
	@XmlElement(name="codeValue", required=false)
	public String getCodeValue() {
		return codeValue;
	}

	public void setCodeValue(String codeValue) {
		this.codeValue = codeValue;
	}
*/
	/*
	@XmlElement(name="fullValue", required=true, nillable=true)
	public String getFullValue() {
		return fullValue;
	}

	public void setFullValue(String fullValue) {
		this.fullValue = fullValue;
	}

	@XmlElement(name="typeOrSource")
	public TypeOrSource getTypeOrSource() {
		return typeOrSource;
	}

	public void setTypeOrSource(TypeOrSource tos) {
		this.typeOrSource = tos;
	}
	*/
	
}
