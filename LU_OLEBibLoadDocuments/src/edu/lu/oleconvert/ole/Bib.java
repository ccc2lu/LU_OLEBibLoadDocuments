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
@Table(name="ole_ds_bib_t")
public class Bib implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1102828622828122306L;
	
	private static DateFormat df = new SimpleDateFormat("yyyyMMdd");
	private static String datestr = df.format(Calendar.getInstance().getTime());
	
	private Long id;
	private String formerId;
	private String fastAdd;
	private String staffOnly;
	private String createdBy;
	private String dateCreated;
	private String updatedBy;
	private String dateUpdated;
	private String status;
	private String statusUpdatedBy;
	private String statusUpdatedDate;
	private String uniqueIdPrefix;
	private String content;
	private List<OLEHoldings> holdings;
	
	public Bib() {
		// Set some default values
		this.setDateCreated(datestr, false);
		this.setStatusUpdatedDate(datestr, false);
		this.setDateUpdated(datestr, false);
		this.setCreatedBy("BulkIngest-User");
		this.setUpdatedBy("BulkIngest-User");
		holdings = new ArrayList<OLEHoldings>();
		//this.setId((long) 0);
		this.uniqueIdPrefix = "wbo";
	}
	
	public Bib(String formerId) {
		this();
		try {
			if ( formerId.substring(0,1).equals("o") ) {
				LU_DBLoadInstances.Log(System.err, "Stripping first 3 characters off of bib ID " + formerId,
						LU_DBLoadInstances.LOG_WARN);
				this.setId(Long.parseLong(formerId.substring(3)));
			} else {
				this.setId(Long.parseLong(formerId));
			}
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to parse bib id from string " + formerId,
					LU_DBLoadInstances.LOG_ERROR);
		}
		this.setFormerId(formerId);
	}
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="bib", cascade=CascadeType.ALL)
	//@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<OLEHoldings> getHoldings() {
		return this.holdings;
	}
	public void setHoldings(List<OLEHoldings> holdings) {
		this.holdings = holdings;
	}
	
	@Id
	//@GeneratedValue
	@Column(name="BIB_ID")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name="FORMER_ID")
	public String getFormerId() {
		return formerId;
	}
	public void setFormerId(String formerId) {
		this.formerId = formerId;
	}
	
	@Column(name="FAST_ADD")
	public String getFastAdd() {
		return fastAdd;
	}
	public void setFastAdd(String fastAdd) {
		this.fastAdd = fastAdd;
	}
	
	@Column(name="STAFF_ONLY")
	public String getStaffOnly() {
		return staffOnly;
	}
	public void setStaffOnly(String staffOnly) {
		this.staffOnly = staffOnly;
	}
	
	@Column(name="CREATED_BY")
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	@Column(name="DATE_CREATED")
	public String getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		try {
			df.parse(dateCreated);
			// if there's no exception, then it's fine, assign the date created
			this.dateCreated = dateCreated;
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set bib record's created date from date string: " + dateCreated,
					LU_DBLoadInstances.LOG_ERROR);
			this.dateCreated = datestr;
		}
	}
	public void setDateCreated(String newdateCreated, boolean check) {
		if ( check ) {
			setDateCreated(newdateCreated);
		} else {
			this.dateCreated = newdateCreated;
		}
	}
	
	@Column(name="UPDATED_BY")
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	@Column(name="DATE_UPDATED")
	public String getDateUpdated() {
		return dateUpdated;
	}
	public void setDateUpdated(String dateUpdated) {
		try {
			df.parse(dateUpdated);
			// if there's no exception, then it's fine, assign the date created
			this.dateUpdated = dateUpdated;
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set bib record's updated date from date string: " + dateUpdated,
					LU_DBLoadInstances.LOG_ERROR);
			this.dateUpdated = datestr;
		}		
	}
	public void setDateUpdated(String newdateUpdated, boolean check) {
		if ( check ) {
			setDateUpdated(newdateUpdated);
		} else {
			this.dateUpdated = newdateUpdated;
		}
	}
	
	@Column(name="STATUS")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Column(name="STATUS_UPDATED_BY")
	public String getStatusUpdatedBy() {
		return statusUpdatedBy;
	}
	public void setStatusUpdatedBy(String statusUpdatedBy) {
		this.statusUpdatedBy = statusUpdatedBy;
	}
	
	@Column(name="STATUS_UPDATED_DATE")
	public String getStatusUpdatedDate() {
		return statusUpdatedDate;
	}
	
	public void setStatusUpdatedDate(String statusUpdatedDate) {
		try {
			df.parse(statusUpdatedDate);
			// if there's no exception, then it's fine, assign the date created
			this.statusUpdatedDate = statusUpdatedDate;
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set bib record's status updated date from date string: " + statusUpdatedDate,
					LU_DBLoadInstances.LOG_ERROR);
			this.statusUpdatedDate = datestr;
		}		
	}
	
	public void setStatusUpdatedDate(String updatedDate, boolean check) {
		if ( check ) {
			setStatusUpdatedDate(updatedDate);
		} else {
			this.statusUpdatedDate = updatedDate;
		}
	}
	
	@Column(name="UNIQUE_ID_PREFIX")
	public String getUniqueIdPrefix() {
		return uniqueIdPrefix;
	}
	public void setUniqueIdPrefix(String uniqueIdPrefix) {
		this.uniqueIdPrefix = uniqueIdPrefix;
	}
	
	@Column(name="CONTENT")
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
}
