package edu.lu.oleconvert.ole;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="ole_ds_holdings_donor_t")
public class OLEHoldingsDonor implements Serializable {

	private Long id;
	private String donorCode;
	private String donorPublicDisplay;
	private String donorNote;
	private OLEHoldings holdings;
	
	public OLEHoldingsDonor() {
		
	}

	@Id
	@GeneratedValue
	@Column(name="ITEM_DONOR_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="DONOR_CODE")
	public String getDonorCode() {
		return donorCode;
	}

	public void setDonorCode(String donorCode) {
		this.donorCode = donorCode;
	}

	@Column(name="DONOR_PUBLIC_DISPLAY")
	public String getDonorPublicDisplay() {
		return donorPublicDisplay;
	}

	public void setDonorPublicDisplay(String donorPublicDisplay) {
		this.donorPublicDisplay = donorPublicDisplay;
	}

	@Column(name="DONOR_NOTE")
	public String getDonorNote() {
		return donorNote;
	}

	public void setDonorNote(String donorNote) {
		this.donorNote = donorNote;
	}

	@OneToOne
	@JoinColumn(name="HOLDINGS_ID")
	public OLEHoldings getHoldings() {
		return holdings;
	}

	public void setHoldings(OLEHoldings holdings) {
		this.holdings = holdings;
	}
	
	
}
