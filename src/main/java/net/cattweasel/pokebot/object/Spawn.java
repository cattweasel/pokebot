package net.cattweasel.pokebot.object;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.DateAdapter;
import net.cattweasel.pokebot.tools.xml.WrappedReferenceAdapter;

@Entity
@Table(name = "db_spawn")
@XmlRootElement(name = "Spawn")
public class Spawn extends PokeObject {
	
	private static final long serialVersionUID = 394496324808164843L;
	
	private Pokemon pokemon;
	private Date disappearTime;
	private Double latitude;
	private Double longitude;
	
	@ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "pokemon", unique = false, nullable = false)
	@XmlElement(name = "Pokemon")
	@XmlJavaTypeAdapter(WrappedReferenceAdapter.class)
	public Pokemon getPokemon() {
		return pokemon;
	}
	
	public void setPokemon(Pokemon pokemon) {
		this.pokemon = pokemon;
	}
	
	@Column(unique = false, nullable = false)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getDisappearTime() {
		return disappearTime == null ? null : new Date(disappearTime.getTime());
	}

	public void setDisappearTime(Date disappearTime) {
		this.disappearTime = disappearTime == null ? null : new Date(disappearTime.getTime());
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
