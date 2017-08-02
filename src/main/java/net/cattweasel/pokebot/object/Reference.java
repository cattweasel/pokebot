package net.cattweasel.pokebot.object;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO implementation for a reference.
 * 
 * @author Benjamin Wesp
 *
 */
@XmlRootElement(name = "Reference")
public class Reference {

	private String clazz;
	private String id;
	private String name;

	public Reference() {
	}

	public Reference(PokeObject o) {
		this.clazz = calculateClassName(o);
		this.id = o.getId();
		this.name = o.getName();
	}

	/**
	 * Returns the class of this reference.
	 * 
	 * @return The class of this reference
	 */
	@XmlAttribute(name = "class")
	public String getClazz() {
		return clazz;
	}

	/**
	 * Sets the class for this reference.
	 * 
	 * @param clazz The class for this reference
	 */
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	/**
	 * Returns the ID of this reference.
	 * 
	 * @return The ID of this reference
	 */
	@XmlAttribute
	public String getId() {
		return id;
	}

	/**
	 * Sets the ID for this reference.
	 * 
	 * @param id The ID for this reference
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the name of this reference.
	 * 
	 * @return The name of this reference
	 */
	@XmlAttribute
	public String getName() {
		return name;
	}

	/**
	 * Sets the name for this reference.
	 * 
	 * @param name The name for this reference
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Overriding the default toString method for a nicer view.
	 * 
	 * @return The formated string to display
	 */
	@Override
	public String toString() {
		return String.format("%s@%s[class = %s, id = %s, name = %s]",
				getClass().getName(), hashCode(), clazz, id, name);
	}

	/**
	 * Private helper method to determine the correct class name for this reference.
	 * 
	 * @param The object for which the class name should be calculated
	 * @return The calculated class name for this reference
	 */
	private String calculateClassName(PokeObject o) {
		String name = o == null ? null : o.getClass().getName();
		if (name != null && name.contains("_")) {
			name = name.split("_")[0];
		}
		return name;
	}
}
