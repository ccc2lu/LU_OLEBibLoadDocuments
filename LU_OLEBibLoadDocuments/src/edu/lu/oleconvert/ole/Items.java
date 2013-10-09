package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="items")
public class Items implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5065130714164665238L;
	private ArrayList<Item> items;
	
	public Items() {
		super();
		items = new ArrayList<Item>();
	}

	@XmlElement(name="item")
	public ArrayList<Item> getItems() {
		return items;
	}

	public void setItems(ArrayList<Item> items) {
		this.items = items;
	}
	
	
}
