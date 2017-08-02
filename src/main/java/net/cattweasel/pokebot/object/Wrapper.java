package net.cattweasel.pokebot.object;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.tools.xml.ReferenceAdapter;

/**
 * POJO implementation for a Wrapper.
 * 
 * @author Benjamin Wesp
 *
 */
@XmlRootElement(name = "Wrapper")
public class Wrapper {

	private PokeObject object;
	
	private static final Logger LOG = Logger.getLogger(Wrapper.class);
	
	public Wrapper() {
	}
	
	public Wrapper(PokeObject object) {
		this.object = object;
	}
	
	/**
	 * Returns the wrapped object as a reference.
	 * 
	 * @return The reference to the desired Object
	 */
	@XmlElement(name = "Reference")
	public Reference getObject() {
		return new Reference(object);
	}
	
	/**
	 * Sets the object as a reference for this wrapper.
	 * 
	 * @param ref The reference to be used
	 */
	public void setObject(Reference ref) {
		try {
			object = new ReferenceAdapter().unmarshal(ref);
		} catch (Exception ex) {
			LOG.error(ex);
		}
	}
}
