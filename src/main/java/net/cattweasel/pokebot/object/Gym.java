package net.cattweasel.pokebot.object;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "db_gym")
@XmlRootElement(name = "Gym")
public class Gym extends PokeObject {
	
	private static final long serialVersionUID = 7281718792637457962L;
	
	private String displayName;
	private Team team;
	private Double latitude;
	private Double longitude;
	private Integer score;
	private Boolean inBatte;
	private Long timeOcuppied;
	private Date raidStart;
	private Date raidEnd;
	private Integer raidLevel;
	private Pokemon raidPokemon;
	private Integer raidCp;
	
	@Column(unique = false, nullable = false)
	@XmlAttribute
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team", unique = false, nullable = true)
	@XmlElement(name = "Team")
	@XmlJavaTypeAdapter(WrappedReferenceAdapter.class)
	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
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

	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	@Column(unique = false, nullable = false)
	@XmlAttribute
	public Boolean getInBatte() {
		return inBatte;
	}

	public void setInBatte(Boolean inBatte) {
		this.inBatte = inBatte;
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Long getTimeOcuppied() {
		return timeOcuppied;
	}

	public void setTimeOcuppied(Long timeOcuppied) {
		this.timeOcuppied = timeOcuppied;
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getRaidStart() {
		return raidStart == null ? null : new Date(raidStart.getTime());
	}

	public void setRaidStart(Date raidStart) {
		this.raidStart = raidStart == null ? null : new Date(raidStart.getTime());
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getRaidEnd() {
		return raidEnd == null ? null : new Date(raidEnd.getTime());
	}

	public void setRaidEnd(Date raidEnd) {
		this.raidEnd = raidEnd == null ? null : new Date(raidEnd.getTime());
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Integer getRaidLevel() {
		return raidLevel;
	}

	public void setRaidLevel(Integer raidLevel) {
		this.raidLevel = raidLevel;
	}

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raid_pokemon", unique = false, nullable = true)
	@XmlElement(name = "RaidPokemon")
	@XmlJavaTypeAdapter(WrappedReferenceAdapter.class)
	public Pokemon getRaidPokemon() {
		return raidPokemon;
	}

	public void setRaidPokemon(Pokemon raidPokemon) {
		this.raidPokemon = raidPokemon;
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Integer getRaidCp() {
		return raidCp;
	}

	public void setRaidCp(Integer raidCp) {
		this.raidCp = raidCp;
	}
}
