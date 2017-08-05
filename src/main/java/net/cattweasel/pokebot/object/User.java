package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
	
	@Column(unique = false, nullable = false)
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
}
