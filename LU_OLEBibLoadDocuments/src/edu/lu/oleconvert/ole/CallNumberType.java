package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ole_ds_call_number_type_t")
public class CallNumberType implements Serializable {

	private Long id;
	private String code;
	private String name;

	public CallNumberType() {
		super();
	}

	public CallNumberType(String code, String name) {
		this();
		this.setCode(code);
		this.setName(name);
	}
	
	@Id
	@GeneratedValue
	@Column(name="CALL_NUMBER_TYPE_ID")
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
	
	
}
