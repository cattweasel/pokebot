package net.cattweasel.pokebot.object;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO implementation for an import container.
 * 
 * @author Benjamin Wesp
 *
 */
@XmlRootElement(name = "ImportContainer")
public class ImportContainer {
	
	private List<ImportAction> actions;
	
	/**
	 * Returns the import actions of this container.
	 * 
	 * @return The actions of this container
	 */
	@XmlElementWrapper(name = "ImportActions")
	@XmlElement(name = "ImportAction")
	public List<ImportAction> getActions() {
		return actions;
	}
	
	/**
	 * Sets the actions for this container.
	 * 
	 * @param actions The actions for this container
	 */
	public void setActions(List<ImportAction> actions) {
		this.actions = actions;
	}
}
