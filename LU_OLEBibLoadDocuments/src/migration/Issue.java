package migration;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="issues")
public class Issue implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7096802193740789377L;

	@Id
	@GeneratedValue
	@Column(name="id")
	private int id;
	
	@Column(name="SERC_ID")
	private String serialControlId;
	@Column(name="PRED_LIBRARY")
	private String library;
	@Column(name="PRED_NAME")
	private String predictionName;
	@Column(name="PRED_NUMERATION")
	private String predictionNumeration;
	@Column(name="PRED_NUM_EXPECTED")
	private String predictionNumExpected;
	@Column(name="PRED_DATE_EXPECTED")
	private String predictionDateExpected;
	@Column(name="PRED_PUB_DATE")
	private String predictionDatePublished;
	@Column(name="PRED_ISSUE_ID_S")
	private String predictionIssueId;
	@Column(name="PRED_DATE_TO_CLAIM")
	private String predictionDateToClaim;
	@Column(name="PRED_DATE_CREATED")
	private String predictionDateCreated;
	@Column(name="RCPT_NUM_REC")
	private String receiptNumberReceived;
	@Column(name="RCPT_DATE")
	private String receiptDateReceived;
	@Column(name="RCPT_NUMBER_S")
	private String receiptNumber;
	@Column(name="RCPT_DATE_CREATED")
	private String receiptDateCreated;
	@Column(name="CLAIM_NUMBER_CLAIMED")
	private String claimNumberClaimed;
	@Column(name="CLAIM_NUMBER_S")
	private String claimNumber;
	@Column(name="CLAIM_DATE_CLAIMED")
	private String claimDateClaimed;
	@Column(name="CLAIM_DATE_TO_SEND")
	private String claimDateToSend;
	@Column(name="CLAIM_TIMES_CLAIMED")
	private String claimTimesCLaimed;
	@Column(name="CLAIM_REASON")
	private String claimReason;
	@Column(name="CLAIM_RESPONSE_DATE")
	private String claimResponseDate;
	@Column(name="CLAIM_DATE_CREATED")
	private String claimDateCreated;
	@Column(name="PRED_DATE_MOD")
	private String predictionDateModified;
	@Column(name="RCPT_DATE_MOD")
	private String receiptDateModified;
	@Column(name="CLAIM_DATE_MOD")
	private String claimDateModified;
	@Column(name="PRED_COMMENT")
	private String predictionComment;
	@Column(name="RCPT_COMMENT")
	private String receiptComment;
	@Column(name="CLAIM_COMMENT")
	private String claimComment;
	
	public Issue() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSerialControlId() {
		return serialControlId;
	}

	public void setSerialControlId(String serialControlId) {
		this.serialControlId = serialControlId;
	}

	public String getLibrary() {
		return library;
	}

	public void setLibrary(String library) {
		this.library = library;
	}

	public String getPredictionName() {
		return predictionName;
	}

	public void setPredictionName(String predictionName) {
		this.predictionName = predictionName;
	}

	public String getPredictionNumeration() {
		return predictionNumeration;
	}

	public void setPredictionNumeration(String predictionNumeration) {
		this.predictionNumeration = predictionNumeration;
	}

	public String getPredictionNumExpected() {
		return predictionNumExpected;
	}

	public void setPredictionNumExpected(String predictionNumExpected) {
		this.predictionNumExpected = predictionNumExpected;
	}

	public String getPredictionDateExpected() {
		return predictionDateExpected;
	}

	public void setPredictionDateExpected(String predictionDateExpected) {
		this.predictionDateExpected = predictionDateExpected;
	}

	public String getPredictionDatePublished() {
		return predictionDatePublished;
	}

	public void setPredictionDatePublished(String predictionDatePublished) {
		this.predictionDatePublished = predictionDatePublished;
	}

	public String getPredictionIssueId() {
		return predictionIssueId;
	}

	public void setPredictionIssueId(String predictionIssueId) {
		this.predictionIssueId = predictionIssueId;
	}

	public String getPredictionDateToClaim() {
		return predictionDateToClaim;
	}

	public void setPredictionDateToClaim(String predictionDateToClaim) {
		this.predictionDateToClaim = predictionDateToClaim;
	}

	public String getPredictionDateCreated() {
		return predictionDateCreated;
	}

	public void setPredictionDateCreated(String predictionDateCreated) {
		this.predictionDateCreated = predictionDateCreated;
	}

	public String getReceiptNumberReceived() {
		return receiptNumberReceived;
	}

	public void setReceiptNumberReceived(String receiptNumberReceived) {
		this.receiptNumberReceived = receiptNumberReceived;
	}

	public String getReceiptDateReceived() {
		return receiptDateReceived;
	}

	public void setReceiptDateReceived(String receiptDateReceived) {
		this.receiptDateReceived = receiptDateReceived;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}

	public String getReceiptDateCreated() {
		return receiptDateCreated;
	}

	public void setReceiptDateCreated(String receiptDateCreated) {
		this.receiptDateCreated = receiptDateCreated;
	}

	public String getClaimNumberClaimed() {
		return claimNumberClaimed;
	}

	public void setClaimNumberClaimed(String claimNumberClaimed) {
		this.claimNumberClaimed = claimNumberClaimed;
	}

	public String getClaimNumber() {
		return claimNumber;
	}

	public void setClaimNumber(String claimNumber) {
		this.claimNumber = claimNumber;
	}

	public String getClaimDateClaimed() {
		return claimDateClaimed;
	}

	public void setClaimDateClaimed(String claimDateClaimed) {
		this.claimDateClaimed = claimDateClaimed;
	}

	public String getClaimDateToSend() {
		return claimDateToSend;
	}

	public void setClaimDateToSend(String claimDateToSend) {
		this.claimDateToSend = claimDateToSend;
	}

	public String getClaimTimesCLaimed() {
		return claimTimesCLaimed;
	}

	public void setClaimTimesCLaimed(String claimTimesCLaimed) {
		this.claimTimesCLaimed = claimTimesCLaimed;
	}

	public String getClaimReason() {
		return claimReason;
	}

	public void setClaimReason(String claimReason) {
		this.claimReason = claimReason;
	}

	public String getClaimResponseDate() {
		return claimResponseDate;
	}

	public void setClaimResponseDate(String claimResponseDate) {
		this.claimResponseDate = claimResponseDate;
	}

	public String getClaimDateCreated() {
		return claimDateCreated;
	}

	public void setClaimDateCreated(String claimDateCreated) {
		this.claimDateCreated = claimDateCreated;
	}

	public String getPredictionDateModified() {
		return predictionDateModified;
	}

	public void setPredictionDateModified(String predictionDateModified) {
		this.predictionDateModified = predictionDateModified;
	}

	public String getReceiptDateModified() {
		return receiptDateModified;
	}

	public void setReceiptDateModified(String receiptDateModified) {
		this.receiptDateModified = receiptDateModified;
	}

	public String getClaimDateModified() {
		return claimDateModified;
	}

	public void setClaimDateModified(String claimDateModified) {
		this.claimDateModified = claimDateModified;
	}

	public String getPredictionComment() {
		return predictionComment;
	}

	public void setPredictionComment(String predictionComment) {
		this.predictionComment = predictionComment;
	}

	public String getReceiptComment() {
		return receiptComment;
	}

	public void setReceiptComment(String receiptComment) {
		this.receiptComment = receiptComment;
	}

	public String getClaimComment() {
		return claimComment;
	}

	public void setClaimComment(String claimComment) {
		this.claimComment = claimComment;
	}
	
	
	
}
