package net.cattweasel.pokebot.object;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.DateAdapter;

@Entity
@Table(name = "db_pokemon")
@XmlRootElement(name = "Pokemon")
public class Pokemon extends PokeObject {

	private static final long serialVersionUID = 7080727075450830557L;
	
	private Integer pokemonId;
	private Date disappearTime;
	private Integer eid;
	private Double latitude;
	private Double longitude;
	
	@Column(unique = false, nullable = false)
	@XmlAttribute
	public Integer getPokemonId() {
		return pokemonId;
	}
	
	public void setPokemonId(Integer pokemonId) {
		this.pokemonId = pokemonId;
	}

	@Column(unique = false, nullable = false)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getDisappearTime() {
		return disappearTime;
	}

	public void setDisappearTime(Date disappearTime) {
		this.disappearTime = disappearTime;
	}

	@Column(unique = false, nullable = false)
	@XmlAttribute
	public Integer getEid() {
		return eid;
	}

	public void setEid(Integer eid) {
		this.eid = eid;
	}

	@Column(unique = false, nullable = false)
	@XmlAttribute
	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	@Column(unique = false, nullable = false)
	@XmlAttribute
	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
}
