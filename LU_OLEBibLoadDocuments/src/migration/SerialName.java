package migration;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="serials_name")
public class SerialName implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1961205458845045711L;

	@Id
	@Column(name="id")
	private Long id;
	
	@Column(name="name")
	private String name;
	
	@ManyToOne
	@JoinColumn(name="serials_id", referencedColumnName="id")
	private Serial serial;
	
	public String getName() {
		return name;
	}

	public void setName(String newname) {
		this.name = newname;
	}

	public Serial getSerial() {
		return serial;
	}

	public void setSerial(Serial serial) {
		this.serial = serial;
	}

	public SerialName() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
}
