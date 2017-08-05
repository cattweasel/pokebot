package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "db_team", uniqueConstraints = { @UniqueConstraint(columnNames = "teamId") })
@XmlRootElement(name = "Team")
public class Team extends PokeObject {

	private static final long serialVersionUID = 8808190452966102941L;
	
	private Integer teamId;
	
	@Column(unique = true, nullable = false)
	@XmlAttribute
	public Integer getTeamId() {
		return teamId;
	}
	
	public void setTeamId(Integer teamId) {
		this.teamId = teamId;
	}
}
