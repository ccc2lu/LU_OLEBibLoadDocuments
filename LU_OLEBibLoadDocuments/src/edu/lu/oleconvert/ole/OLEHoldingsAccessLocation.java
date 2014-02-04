package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ole_ds_holdings_access_loc_t")
public class OLEHoldingsAccessLocation implements Serializable {
	
	private Long id;
	private OLEHoldings oleHoldings;
	private AccessLocation accessLocation;

	public OLEHoldingsAccessLocation() {
		super();
	}
	
	@Id
	@GeneratedValue
	@Column(name="HOLDINGS_ACCESS_LOCATION_ID")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne
	@JoinColumn(name="HOLDINGS_ID")
	public OLEHoldings getOleHoldings() {
		return oleHoldings;
	}
	public void setOleHoldings(OLEHoldings oleHoldings) {
		this.oleHoldings = oleHoldings;
	}
	
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="ACCESS_LOCATION_ID")
	public AccessLocation getAccessLocation() {
		return accessLocation;
	}
	public void setAccessLocation(AccessLocation accessLocation) {
		this.accessLocation = accessLocation;
	}
	
}
	