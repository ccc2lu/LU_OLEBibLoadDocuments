package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Type;

@Entity
@Table(name="ole_ds_statistical_searching_t")
@XmlType(name="statisticalSearchingCode", propOrder={"codeValue", "fullValue", "typeOrSource"})
public class StatisticalSearchingCode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5015280807896678438L;

	private Long id;
	private String code;
	private String name;
	
	public StatisticalSearchingCode() {
		super();
		code = "";
		name = "";
	}

	@Id
	@GeneratedValue
	@Column(name="STATISTICAL_SEARCHING_ID")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name="CODE")
	@XmlElement(name="codeValue", required=true, nillable=true)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name="NAME")
	@XmlElement(name="fullValue", required=true, nillable=true)	
	public String getName() {
		return name;
	}

	public void setName(String fullValue) {
		this.name = fullValue;
	}

	/*
	@XmlElement(name="typeOrSource")
	public TypeOrSource getTypeOrSource() {
		return typeOrSource;
	}

	public void setTypeOrSource(TypeOrSource typeOrSource) {
		this.typeOrSource = typeOrSource;
	}
	*/
	
	
}
