package edu.lu.oleconvert.ole;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ole_ds_access_location_t")
public class AccessLocation {
	private Long id;
	private String code;
	private String name;
	
	public AccessLocation() {
		super();
	}
	public AccessLocation(String code, String name) {
		this();
		this.setCode(code);
		this.setName(name);
	}
	
	@Id
	@GeneratedValue
	@Column(name="ACCESS_LOCATION_ID")
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
