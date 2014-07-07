package migration;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="callnumbers")
public class SirsiCallNumber implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7360638619314945547L;

	public SirsiCallNumber() {
		super();
		this.Id = new SirsiCallNumberID();
	}
	
	@EmbeddedId 
	SirsiCallNumberID Id;
	
	@Column(name="analytic_pos")
	int analytic_pos;
	@Column(name="level")
	String level;
	@Column(name="num_copies")
	int num_copies;
	@Column(name="num_call_holds")
	int num_call_holds;
	@Column(name="classification")
	String classification;
	@Column(name="num_reserve_control_recs")
	int num_reserve_control_recs;
	@Column(name="num_academic_reserves")
	int num_academic_reserves;
	@Column(name="library")
	String library;
	@Column(name="num_visible_copies")
	int num_visible_copies;
	@Column(name="shadowed")
	String shadowed;
	@Column(name="shelving_key")
	String shelving_key; 
	@Column(name="call_number")
	String call_number;
	@Column(name="analytics")
	String analytics;
	@Column(name="parent_cat_key")
	int parent_cat_key;
	@Column(name="parent_callnum_key")
	int parent_callnum_key;
	@Column(name="creator_access")
	String creator_access;
	@Column(name="bound_create_date")
	String bound_create_date;
	
	public int getParent_cat_key() {
		return parent_cat_key;
	}
	public void setParent_cat_key(int parent_cat_key) {
		this.parent_cat_key = parent_cat_key;
	}
	public int getParent_callnum_key() {
		return parent_callnum_key;
	}
	public void setParent_callnum_key(int parent_callnum_key) {
		this.parent_callnum_key = parent_callnum_key;
	}
	public String getCreator_access() {
		return creator_access;
	}
	public void setCreator_access(String creator_access) {
		this.creator_access = creator_access;
	}
	public String getBound_create_date() {
		return bound_create_date;
	}
	public void setBound_create_date(String bound_create_date) {
		this.bound_create_date = bound_create_date;
	}
	public SirsiCallNumberID getId() {
		return this.Id;
	}
	public void setId(SirsiCallNumberID scnid) {
		this.Id = scnid;
	}
	public void setId(int catkey, int callnumkey) {
		this.Id.cat_key = catkey;
		this.Id.callnum_key = callnumkey;
	}
	public int getCat_key() {
		return this.Id.cat_key;
	}
	public void setCat_key(int cat_key) {
		this.Id.cat_key = cat_key;
	}
	public int getCallnum_key() {
		return this.Id.callnum_key;
	}
	public void setCallnum_key(int callnum_key) {
		this.Id.callnum_key = callnum_key;
	}
	public int getAnalytic_pos() {
		return analytic_pos;
	}
	public void setAnalytic_pos(int analytic_pos) {
		this.analytic_pos = analytic_pos;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public int getNum_copies() {
		return num_copies;
	}
	public void setNum_copies(int num_copies) {
		this.num_copies = num_copies;
	}
	public int getNum_call_holds() {
		return num_call_holds;
	}
	public void setNum_call_holds(int num_call_holds) {
		this.num_call_holds = num_call_holds;
	}
	public String getClassification() {
		return classification;
	}
	public void setClassification(String classification) {
		this.classification = classification;
	}
	public int getNum_reserve_control_recs() {
		return num_reserve_control_recs;
	}
	public void setNum_reserve_control_recs(int num_reserve_control_recs) {
		this.num_reserve_control_recs = num_reserve_control_recs;
	}
	public int getNum_academic_reserves() {
		return num_academic_reserves;
	}
	public void setNum_academic_reserves(int num_academic_reserves) {
		this.num_academic_reserves = num_academic_reserves;
	}
	public String getLibrary() {
		return library;
	}
	public void setLibrary(String library) {
		this.library = library;
	}
	public int getNum_visible_copies() {
		return num_visible_copies;
	}
	public void setNum_visible_copies(int num_visible_copies) {
		this.num_visible_copies = num_visible_copies;
	}
	public String getShadowed() {
		return shadowed;
	}
	public void setShadowed(String shadowed) {
		this.shadowed = shadowed;
	}
	public String getShelving_key() {
		return shelving_key;
	}
	public void setShelving_key(String shelving_key) {
		this.shelving_key = shelving_key;
	}
	public String getCall_number() {
		return call_number;
	}
	public void setCall_number(String call_number) {
		this.call_number = call_number;
	}
	public String getAnalytics() {
		return analytics;
	}
	public void setAnalytics(String analytics) {
		this.analytics = analytics;
	}
	
	
}
