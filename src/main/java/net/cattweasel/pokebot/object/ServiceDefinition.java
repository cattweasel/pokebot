package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO implementation for a service definition.
 * 
 * @author Benjamin Wesp
 *
 */
@Entity
@Table(name = "db_service_definition")
@XmlRootElement(name = "ServiceDefinition")
public class ServiceDefinition extends PokeObject {

	private static final long serialVersionUID = 2307060443524218396L;
	
	private String _executor;
	private int _interval;
	private Attributes<String, Object> _attributes;

	/**
	 * Returns the executor of this service definition.
	 * 
	 * @return The executor of this service definition
	 */
	@Column(unique = false, nullable = false, length = 255)
	@XmlAttribute
	public String getExecutor() {
		return this._executor;
	}

	/**
	 * Sets the executor for this service definition.
	 * 
	 * @param s The executor for this service definition
	 */
	public void setExecutor(String s) {
		this._executor = s;
	}

	/**
	 * Returns the execution interval of this service definition.
	 * 
	 * @return The execution interval of this service definition
	 */
	@Column(name = "execution_interval", unique = false, nullable = false)
	@XmlAttribute
	public int getInterval() {
		return this._interval;
	}

	/**
	 * Sets the execution interval for this service definition
	 * 
	 * @param i The execution interval for this service definition
	 */
	public void setInterval(int i) {
		this._interval = i;
	}

	/**
	 * Returns the attributes of this service definition.
	 * 
	 * @return The attributes of this service definition
	 */
	@Lob
	@Column(unique = false, nullable = true)
	@XmlElement(name = "Attributes")
	public Attributes<String, Object> getAttributes() {
		return this._attributes;
	}

	/**
	 * Sets the attributes for this service definition.
	 * 
	 * @param a The attributes for this service definition
	 */
	public void setAttributes(Attributes<String, Object> a) {
		this._attributes = a;
	}

	/**
	 * Resolve an retrieve an attribute by its key name.
	 * 
	 * @param name The name of the attribute
	 * @return The attribute value if found - null otherwise
	 */
	public Object get(String name) {
		return this._attributes != null ? this._attributes.get(name) : null;
	}

	/**
	 * Put an attribute into the map of defined attributes.
	 * 
	 * @param name The name of the attribute
	 * @param value The value of the attribute
	 */
	public void put(String name, Object value) {
		if (this._attributes == null) {
			this._attributes = new Attributes<String, Object>();
		}
		if (value == null) {
			this._attributes.remove(name);
		} else {
			this._attributes.put(name, value);
		}
	}

	/**
	 * Resolve an retrieve an attribute by its key name as string.
	 * 
	 * @param name The name of the attribute
	 * @return The attribute value as string if available - null otherwise
	 */
	public String getString(String name) {
		return this._attributes != null ? this._attributes.getString(name) : null;
	}

	/**
	 * Resolve an retrieve an attribute by its key name as integer.
	 * 
	 * @param name The name of the attribute
	 * @return The attribute value as integer if available - null otherwise
	 */
	public int getInt(String name) {
		return this._attributes != null ? this._attributes.getInt(name) : 0;
	}

	/**
	 * Resolve an retrieve an attribute by its key name as boolean.
	 * 
	 * @param name The name of the attribute
	 * @return The attribute value as boolean if available - null otherwise
	 */
	public boolean getBoolean(String name) {
		return this._attributes != null && this._attributes.getBoolean(name);
	}
}
