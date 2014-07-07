package migration;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SirsiCallNumberID implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8208140324229382924L;
	
	public SirsiCallNumberID() {
		super();
	}
	public SirsiCallNumberID(int catkey, int callnumkey) {
		this();
		this.cat_key = catkey;
		this.callnum_key = callnumkey;
	}

	@Column(name="cat_key")
	int cat_key;
	@Column(name="callnum_key")
	int callnum_key;

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
	
}