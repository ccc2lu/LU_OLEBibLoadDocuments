package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Id;

@Entity
@Table(name="krns_doc_hdr_t")
public class KRNSDoc implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7594971457542003992L;

	public KRNSDoc() {
		super();
		this.versionNumber = 1; 
		this.setObjId(UUID.randomUUID().toString());
	}
	
	public KRNSDoc(FDoc doc) {
		this();
		//this.setfDocNumber(doc.getId());
		this.setfDoc(doc);
		this.setDesc(doc.getTitle());
	}
	
	@Id
	@Column(name="DOC_HDR_ID")
	private Long Id;
	
	@MapsId
	@OneToOne(mappedBy="krnsDoc")
	@JoinColumn(name="DOC_HDR_ID")
	private FDoc fDoc;
		
	@Column(name="OBJ_ID")
	private String objId;
	
	@Column(name="VER_NBR")
	private int versionNumber;
	
	@Column(name="FDOC_DESC")
	private String desc;

	public FDoc getfDoc() {
		return fDoc;
	}

	public void setfDoc(FDoc fDoc) {
		this.fDoc = fDoc;
	}
	
	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}
	
	
	
}
