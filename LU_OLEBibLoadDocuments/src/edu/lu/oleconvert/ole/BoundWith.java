package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.mysql.jdbc.Clob;

import edu.lu.oleconvert.LU_BuildOLELoadDocs;
import edu.lu.oleconvert.LU_DBLoadInstances;

@Entity
@Table(name="ole_ds_bib_holdings_t")
public class BoundWith implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3541568315365363323L;

	Long id;
	Long bibId;
	Long holdingsId;
	
	public BoundWith() {
		super();
	}

	@Id
	@GeneratedValue
	@Column(name="BIB_HOLDINGS_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="BIB_ID")
	public Long getBibId() {
		return bibId;
	}

	public void setBibId(Long bibId) {
		this.bibId = bibId;
	}

	@Column(name="HOLDINGS_ID")
	public Long getHoldingsId() {
		return holdingsId;
	}

	public void setHoldingsId(Long holdingsId) {
		this.holdingsId = holdingsId;
	}
	
	
}
