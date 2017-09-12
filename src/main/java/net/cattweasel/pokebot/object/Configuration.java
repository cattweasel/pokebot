package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "db_configuration")
@XmlRootElement(name = "Configuration")
public class Configuration extends PokeObject {

	public static final String SYSTEM_CONFIGURATION = "SystemConfiguration";
	
	private static final long serialVersionUID = 2297429264602301850L;

	private Attributes<String, Object> attributes;
	
	@Lob
	@Column(unique = false, nullable = true)
	@XmlElement(name = "Attributes")
	public Attributes<String, Object> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Attributes<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public Object get(String key) {
		return attributes == null ? null : attributes.get(key);
	}
	
	public void put(String key, Object value) {
		if (attributes == null) {
			attributes = new Attributes<String, Object>();
		}
		attributes.put(key, value);
	}
}
