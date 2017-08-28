package net.cattweasel.pokebot.object;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.ReferenceAdapter;

@Entity
@Table(name = "db_user")
@XmlRootElement(name = "User")
public class User extends PokeObject {

	private static final long serialVersionUID = 7254715159344245374L;
	
	private String username;
	private String firstname;
	private String lastname;
	private String languageCode;
	private Attributes<String, Object> settings;
	private List<Capability> capabilities;
	private Boolean banned;
	
	@Column(unique = false, nullable = true)
	@XmlAttribute
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String displayName) {
		this.username = displayName;
	}
	
	@Column(unique = false, nullable = true)
	@XmlAttribute
	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	@Column(unique = false, nullable = true)
	@XmlAttribute
	public String getLastname() {
		return lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	@Column(unique = false, nullable = true)
	@XmlAttribute
	public String getLanguageCode() {
		return languageCode;
	}
	
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	
	@Lob
	@Column(unique = false, nullable = true)
	@XmlElement(name = "Settings")
	public Attributes<String, Object> getSettings() {
		return settings;
	}

	public void setSettings(Attributes<String, Object> settings) {
		this.settings = settings;
	}
	
	/**
	 * Returns the capabilities of this user.
	 * 
	 * @return A list of capabilities
	 */
	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "db_user_capability",
			joinColumns = {  @JoinColumn(name = "user", nullable = false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "capability",  nullable = false, updatable = false) })
	@XmlElementWrapper(name = "Capabilities")
	@XmlElement(name = "Reference")
	@XmlJavaTypeAdapter(ReferenceAdapter.class)
	public List<Capability> getCapabilities() {
		return capabilities;
	}
	
	/**
	 * Sets the capabilities for this user.
	 * 
	 * @param capabilities A list of capabilities
	 */
	public void setCapabilities(List<Capability> capabilities) {
		this.capabilities = capabilities;
	}
	
	public boolean hasCapability(String name) {
		boolean result = false;
		if (name != null && capabilities != null && !capabilities.isEmpty()) {
			for (Capability c : capabilities) {
				if (name.equals(c.getName())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Boolean isBanned() {
		return banned;
	}
	
	public void setBanned(Boolean banned) {
		this.banned = banned;
	}
}
