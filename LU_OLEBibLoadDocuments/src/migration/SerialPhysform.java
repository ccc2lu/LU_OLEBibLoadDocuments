package migration;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="serials_physform")
public class SerialPhysform implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8834668730802860296L;
	
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(name="physform")
	private String physform;
	
	@ManyToOne
	@JoinColumn(name="serial_id", referencedColumnName="id")
	private Serial serial;
	
	public SerialPhysform() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPhysform() {
		return physform;
	}

	public void setPhysform(String physform) {
		this.physform = physform;
	}

	public Serial getSerial() {
		return serial;
	}

	public void setSerial(Serial serial) {
		this.serial = serial;
	}

	
}