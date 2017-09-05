package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.WrappedReferenceAdapter;

@Entity
@Table(name = "db_profile")
@XmlRootElement(name = "Profile")
public class Profile extends PokeObject {
	
	private static final long serialVersionUID = 5075884503920215350L;
	
	private User user;
	private String displayName;
	private Attributes<String, Object> settings;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user", unique = false, nullable = false)
	@XmlElement(name = "User")
	@XmlJavaTypeAdapter(WrappedReferenceAdapter.class)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@Column(unique = false, nullable = false)
	@XmlAttribute
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
