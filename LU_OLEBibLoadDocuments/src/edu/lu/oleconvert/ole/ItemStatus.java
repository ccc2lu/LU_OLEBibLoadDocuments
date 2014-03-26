package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import edu.lu.oleconvert.LU_DBLoadInstances;

@Entity
@Table(name="ole_ds_item_status_t")
public class ItemStatus implements Serializable {

	private Long id;
	//private String code;
	private String name;
	private Deliver_ItemStatus deliverStatus;
	
	public ItemStatus() {
		super();
	}

	public ItemStatus(String code, String name) {
		this();
//		this.setCode(code);
		this.setName(name);
		this.setDeliverStatus(code,  name);
	}
	
	@Id
	@GeneratedValue
	@Column(name="ITEM_STATUS_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="CODE", referencedColumnName="ITEM_AVAIL_STAT_CD")
	public Deliver_ItemStatus getDeliverStatus() {
		return deliverStatus;
	}
	public void setDeliverStatus(Deliver_ItemStatus dis) {
		this.deliverStatus = dis;
	}
	public void setDeliverStatus(String code, String name) {
		Deliver_ItemStatus status;
		TypedQuery<Deliver_ItemStatus> query = LU_DBLoadInstances.em.createQuery("SELECT s FROM Deliver_ItemStatus s WHERE s.code='" + code + "'", Deliver_ItemStatus.class);
		query.setHint("org.hibernate.cacheable", true);
		List<Deliver_ItemStatus> results = query.getResultList();
		if ( results.size() == 0 ) {
			//System.out.println("Creating new item type with code " + code);
			status = new Deliver_ItemStatus(code, name);
		} else {
			status = results.get(0);
			//System.out.println("Fetched existing item type with code " + type.getCode());
		}		
		this.setDeliverStatus(status);		
	}
	
	/*
	@Column(name="CODE")
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	*/
	
	@Column(name="NAME")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
