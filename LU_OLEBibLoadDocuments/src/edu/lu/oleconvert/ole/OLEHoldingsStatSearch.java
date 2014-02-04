package edu.lu.oleconvert.ole;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="ole_ds_holdings_stat_search_t")
public class OLEHoldingsStatSearch {

	private Long id;
	private OLEHoldings oleHoldings;
	private StatisticalSearchingCode statSearch;
	
	public OLEHoldingsStatSearch() {
		
	}

	@Id
	@GeneratedValue
	@Column(name="HOLDINGS_STAT_SEARCH_ID")
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
	@JoinColumn(name="STAT_SEARCH_CODE_ID")
	public StatisticalSearchingCode getStatSearch() {
		return statSearch;
	}
	public void setStatSearch(StatisticalSearchingCode statSearch) {
		this.statSearch = statSearch;
	}
	
	
}
