package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Embeddable
@XmlType(name="items")
public class Items implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5065130714164665238L;
	private List<Item> items;
	
	public Items() {
		super();
		items = new ArrayList<Item>();
	}

	@XmlElement(name="item")
	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	
}
