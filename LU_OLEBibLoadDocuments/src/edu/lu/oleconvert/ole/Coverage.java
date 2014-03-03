package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.lu.oleconvert.LU_DBLoadInstances;

@Entity
@Table(name="ole_ds_coverage_t")
public class Coverage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1150999036452449230L;

	private Long id;
	private OLEHoldings oleHoldings;
	private String startDate;
	private String startVolume;
	private String startIssue;
	private String endDate;
	private String endVolume;
	private String endIssue;
	
	public Coverage() {
		super();
	}

	@Id
	@GeneratedValue
	@Column(name="EHOLDINGS_COVERAGE_ID")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name="HOLDINGS_ID")
	public OLEHoldings getOLEHoldings() {
		return oleHoldings;
	}
	public void setOLEHoldings(OLEHoldings holdings) {
		this.oleHoldings = holdings;
	}

	@Column(name="COVERAGE_START_DATE")
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String datestr = df.format(Calendar.getInstance().getTime());
		try {
			df.parse(startDate);
			// if there's no exception, then it's fine, assign the date created
			this.startDate = startDate;
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set coverage record's start date from date string: " + startDate,
					LU_DBLoadInstances.LOG_ERROR);
			this.startDate = null;
		}
	}
	
	@Column(name="COVERAGE_START_VOLUME")
	public String getStartVolume() {
		return startVolume;
	}
	public void setStartVolume(String startVolume) {
		this.startVolume = startVolume;
	}

	@Column(name="COVERAGE_START_ISSUE")
	public String getStartIssue() {
		return startIssue;
	}
	public void setStartIssue(String startIssue) {
		this.startIssue = startIssue;
	}

	@Column(name="COVERAGE_END_DATE")
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String datestr = df.format(Calendar.getInstance().getTime());
		try {
			df.parse(endDate);
			// if there's no exception, then it's fine, assign the date created
			this.endDate = endDate;
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set coverage record's end date from date string: " + endDate,
					LU_DBLoadInstances.LOG_ERROR);
			this.endDate = null;
		}
	}

	@Column(name="COVERAGE_END_VOLUME")
	public String getEndVolume() {
		return endVolume;
	}
	public void setEndVolume(String endVolume) {
		this.endVolume = endVolume;
	}

	@Column(name="COVERAGE_END_ISSUE")
	public String getEndIssue() {
		return endIssue;
	}
	public void setEndIssue(String endIssue) {
		this.endIssue = endIssue;
	}

}
