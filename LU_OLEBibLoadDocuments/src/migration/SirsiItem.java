package migration;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="items")
public class SirsiItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6892807894328203312L;
	public SirsiItem() {
		super();
	}
	
	@Column(name="cat_key")
	int cat_key;
	@Column(name="callnum_key")
	int callnum_key;
	@Column(name="item_key")
	int item_key;
	@Column(name="last_used_date")
	String last_used_date; 
	@Column(name="num_bills")
	int num_bills; 
	@Column(name="num_charges")
	int num_charges;
	@Column(name="num_total_charges")
	int num_total_charges; 
	@Column(name="first_created_date")
	String first_created_date;
	@Column(name="num_holds")
	int num_holds;
	@Column(name="house_charge")
	int house_charge;
	@Column(name="home_location")
	String home_location;
	@Column(name="curr_location")
	String curr_location;
	@Column(name="last_changed_date")
	String last_changed_date;
	@Column(name="permanent")
	String permanent;
	@Column(name="price")
	int price;
	@Column(name="res_type")
	int res_type;
	@Column(name="last_user_key")
	int last_user_key;
	@Column(name="type")
	String type;
	@Column(name="recirc_flag")
	String recirc_flag;
	@Column(name="inventoried_date")
	String inventoried_date;
	@Column(name="times_inventoried")
	int times_inventoried;
	@Column(name="library")
	String library;
	@Column(name="hold_key")
	int hold_key;
	@Column(name="last_discharged_date")
	String last_discharged_date;
	@Column(name="accountability")
	String accountability; 
	@Column(name="shadowed")
	String shadowed;
	@Column(name="distribution_key")
	String distribution_key;
	@Column(name="transit_status")
	String transit_status;
	@Column(name="reserve_status")
	String reserve_status;
	@Column(name="pieces")
	int pieces;
	@Column(name="media_desk")
	String media_desk;
	@Id
	@Column(name="barcode")
	String barcode;
	@Column(name="num_comments")
	int num_comments;
	public int getCat_key() {
		return cat_key;
	}
	public void setCat_key(int cat_key) {
		this.cat_key = cat_key;
	}
	public int getCallnum_key() {
		return callnum_key;
	}
	public void setCallnum_key(int callnum_key) {
		this.callnum_key = callnum_key;
	}
	public int getItem_key() {
		return item_key;
	}
	public void setItem_key(int item_key) {
		this.item_key = item_key;
	}
	public String getLast_used_date() {
		return last_used_date;
	}
	public void setLast_used_date(String last_used_date) {
		this.last_used_date = last_used_date;
	}
	public int getNum_bills() {
		return num_bills;
	}
	public void setNum_bills(int num_bills) {
		this.num_bills = num_bills;
	}
	public int getNum_charges() {
		return num_charges;
	}
	public void setNum_charges(int num_charges) {
		this.num_charges = num_charges;
	}
	public int getNum_total_charges() {
		return num_total_charges;
	}
	public void setNum_total_charges(int num_total_charges) {
		this.num_total_charges = num_total_charges;
	}
	public String getFirst_created_date() {
		return first_created_date;
	}
	public void setFirst_created_date(String first_created_date) {
		this.first_created_date = first_created_date;
	}
	public int getNum_holds() {
		return num_holds;
	}
	public void setNum_holds(int num_holds) {
		this.num_holds = num_holds;
	}
	public int getHouse_charge() {
		return house_charge;
	}
	public void setHouse_charge(int house_charge) {
		this.house_charge = house_charge;
	}
	public String getHome_location() {
		return home_location;
	}
	public void setHome_location(String home_location) {
		this.home_location = home_location;
	}
	public String getCurr_location() {
		return curr_location;
	}
	public void setCurr_location(String curr_location) {
		this.curr_location = curr_location;
	}
	public String getLast_changed_date() {
		return last_changed_date;
	}
	public void setLast_changed_date(String last_changed_date) {
		this.last_changed_date = last_changed_date;
	}
	public String getPermanent() {
		return permanent;
	}
	public void setPermanent(String permanent) {
		this.permanent = permanent;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public int getRes_type() {
		return res_type;
	}
	public void setRes_type(int res_type) {
		this.res_type = res_type;
	}
	public int getLast_user_key() {
		return last_user_key;
	}
	public void setLast_user_key(int last_user_key) {
		this.last_user_key = last_user_key;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRecirc_flag() {
		return recirc_flag;
	}
	public void setRecirc_flag(String recirc_flag) {
		this.recirc_flag = recirc_flag;
	}
	public String getInventoried_date() {
		return inventoried_date;
	}
	public void setInventoried_date(String inventoried_date) {
		this.inventoried_date = inventoried_date;
	}
	public int getTimes_inventoried() {
		return times_inventoried;
	}
	public void setTimes_inventoried(int times_inventoried) {
		this.times_inventoried = times_inventoried;
	}
	public String getLibrary() {
		return library;
	}
	public void setLibrary(String library) {
		this.library = library;
	}
	public int getHold_key() {
		return hold_key;
	}
	public void setHold_key(int hold_key) {
		this.hold_key = hold_key;
	}
	public String getLast_discharged_date() {
		return last_discharged_date;
	}
	public void setLast_discharged_date(String last_discharged_date) {
		this.last_discharged_date = last_discharged_date;
	}
	public String getAccountability() {
		return accountability;
	}
	public void setAccountability(String accountability) {
		this.accountability = accountability;
	}
	public String getShadowed() {
		return shadowed;
	}
	public void setShadowed(String shadowed) {
		this.shadowed = shadowed;
	}
	public String getDistribution_key() {
		return distribution_key;
	}
	public void setDistribution_key(String distribution_key) {
		this.distribution_key = distribution_key;
	}
	public String getTransit_status() {
		return transit_status;
	}
	public void setTransit_status(String transit_status) {
		this.transit_status = transit_status;
	}
	public String getReserve_status() {
		return reserve_status;
	}
	public void setReserve_status(String reserve_status) {
		this.reserve_status = reserve_status;
	}
	public int getPieces() {
		return pieces;
	}
	public void setPieces(int pieces) {
		this.pieces = pieces;
	}
	public String getMedia_desk() {
		return media_desk;
	}
	public void setMedia_desk(String media_desk) {
		this.media_desk = media_desk;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public int getNum_comments() {
		return num_comments;
	}
	public void setNum_comments(int num_comments) {
		this.num_comments = num_comments;
	}
	
}
