package net.cattweasel.pokebot.object;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.WrappedReferenceAdapter;

@Entity
@Table(name = "db_onetime_link")
@XmlRootElement(name = "OnetimeLink")
public class OnetimeLink extends PokeObject {

	private static final long serialVersionUID = 8166813154304014135L;
	
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", unique = false, nullable = false)
	@XmlElement(name = "User")
	@XmlJavaTypeAdapter(WrappedReferenceAdapter.class)
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
}
