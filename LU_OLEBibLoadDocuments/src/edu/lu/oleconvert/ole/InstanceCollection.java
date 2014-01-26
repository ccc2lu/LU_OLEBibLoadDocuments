
package edu.lu.oleconvert.ole;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="instanceCollection")
@XmlType(name="instanceCollection")
public class InstanceCollection implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 281402036232987261L;

	private ArrayList<Instance> instances;
	//private Instance instance;
	
	public InstanceCollection() {
		super();
		instances = new ArrayList<Instance>();
	}

	@XmlElement(name="instance")
	public ArrayList<Instance> getInstances() {
		return instances;
	}

	public void setInstances(ArrayList<Instance> myInstances) {
		this.instances = myInstances;
	}
	
	
}
