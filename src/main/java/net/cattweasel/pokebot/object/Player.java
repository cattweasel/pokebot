package net.cattweasel.pokebot.object;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.WrappedReferenceAdapter;

@Entity
@Table(name = "db_player")
@XmlRootElement(name = "Player")
public class Player extends PokeObject {

	private static final long serialVersionUID = -6224787576181683935L;
	
	private Team team;
	
	@ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "team", unique = false, nullable = false)
	@XmlElement(name = "Team")
	@XmlJavaTypeAdapter(WrappedReferenceAdapter.class)
	public Team getTeam() {
		return team;
	}
	
	public void setTeam(Team team) {
		this.team = team;
	}
}
