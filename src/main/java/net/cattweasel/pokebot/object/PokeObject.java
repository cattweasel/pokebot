package net.cattweasel.pokebot.object;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.GenericGenerator;

import net.cattweasel.pokebot.tools.xml.AbstractXmlObject;
import net.cattweasel.pokebot.tools.xml.DateAdapter;

/**
 * POJO superclass for all API related objects.
 * 
 * @author Benjamin Wesp
 *
 */
@MappedSuperclass
public class PokeObject extends AbstractXmlObject implements Serializable {

	private static final long serialVersionUID = 7976793253699960999L;
	
	private String id;
	private String name;
	private Date created;
	private Date modified;

	/**
	 * Returns the internal ID of this object.
	 * 
	 * @return The internal ID of this object
	 */
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "uuid", unique = true, length = 36)
	@XmlAttribute
	public String getId() {
		return id;
	}

	/**
	 * Sets the internal ID for this object.
	 * 
	 * @param id The internal ID for this object
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the internal name of this object.
	 * 
	 * @return The internal name of this object
	 */
	@Column(unique = true, nullable = false, length = 128)
	@XmlAttribute
	public String getName() {
		return name;
	}

	/**
	 * Sets the internal name for this object.
	 * 
	 * @param name The internal name for this object
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the date when this object was created.
	 * 
	 * @return The date when this object was created
	 */
	@Column(unique = false, nullable = false)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getCreated() {
		return created == null ? null : new Date(created.getTime());
	}

	/**
	 * Sets the date when this object was created.
	 * 
	 * @param created The date when this object was created
	 */
	public void setCreated(Date created) {
		this.created = created == null ? null : new Date(created.getTime());
	}

	/**
	 * Returns the date when this object was modified the last time.
	 * 
	 * @return The date when this object was modified the last time
	 */
	@Column(unique = false, nullable = true)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getModified() {
		return modified == null ? null : new Date(modified.getTime());
	}

	/**
	 * Sets the date when this object was modified the last time.
	 * 
	 * @param modified The date when this object was modified the last time
	 */
	public void setModified(Date modified) {
		this.modified = modified == null ? null : new Date(modified.getTime());
	}
	
	/**
	 * Overriding the default toString method for a nicer view.
	 * 
	 * @return The formated string to display
	 */
	@Override
	public String toString() {
		return String.format("%s[id = %s, name = %s]", getClass().getSimpleName(), getId(), getName());
	}
}
