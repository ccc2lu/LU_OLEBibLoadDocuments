package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ole_ds_holdings_access_uri_t")
public class AccessURI implements Serializable {


	private Long id;
	private String text;
	private String uri;
	private OLEHoldings oleHoldings;
	
	public AccessURI() {
		
	}

	@Id
	@GeneratedValue
	@Column(name="ACCESS_URI_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="TEXT")
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@ManyToOne
	@JoinColumn(name="HOLDINGS_ID")
	public OLEHoldings getOleHoldings() {
		return oleHoldings;
	}

	public void setOleHoldings(OLEHoldings oleHoldings) {
		this.oleHoldings = oleHoldings;
	}

	@Column(name="URI")
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
