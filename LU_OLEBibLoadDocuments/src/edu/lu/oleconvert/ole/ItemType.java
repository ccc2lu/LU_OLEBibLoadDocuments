package edu.lu.oleconvert.ole;


import java.io.Serializable;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.lu.oleconvert.LU_BuildInstance;
import edu.lu.oleconvert.LU_DBLoadInstances;

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
	//private String code;
	private String name;
	private Deliver_ItemType deliverType;
	
	public ItemType() {
		super();		
	}
	
	public ItemType(String code, String name) {
		this();
		//this.setCode(code);
		this.setName(name);
		this.setDeliverType(code, name);
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

	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="CODE", referencedColumnName="ITM_TYP_CD")
	public Deliver_ItemType getDeliverType() {
		return deliverType;
	}
	public void setDeliverType(Deliver_ItemType dit) {
		this.deliverType = dit;
	}
	public void setDeliverType(String code, String name) {
		Deliver_ItemType type;
		TypedQuery<Deliver_ItemType> query = LU_BuildInstance.ole_em.createQuery("SELECT t FROM Deliver_ItemType t WHERE t.code='" + code + "'", Deliver_ItemType.class);
		query.setHint("org.hibernate.cacheable", true);
		List<Deliver_ItemType> results = query.getResultList();
		if ( results.size() == 0 ) {
			//System.out.println("Creating new item type with code " + code);
			type = new Deliver_ItemType(code, name);
		} else {
			type = results.get(0);
			//System.out.println("Fetched existing item type with code " + type.getCode());
		}		
		this.setDeliverType(type);		
	}
	
	/*
	@Column(name="CODE")
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	*/
	
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
