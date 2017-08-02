package net.cattweasel.pokebot.object;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO implementation for an import action.
 * 
 * @author Benjamin Wesp
 *
 */
@XmlRootElement(name = "ImportAction")
public class ImportAction {
	
	private String location;
	
	public ImportAction() {
	}
	
	public ImportAction(String location) {
		this.location = location;
	}
	
	/**
	 * Returns the location of this import action.
	 * 
	 * @return The location of this import action
	 */
	@XmlAttribute
	public String getLocation() {
		return location;
	}
	
	/**
	 * Sets the location for this import action.
	 * 
	 * @param location The location for this import action
	 */
	public void setLocation(String location) {
		this.location = location;
	}
}
