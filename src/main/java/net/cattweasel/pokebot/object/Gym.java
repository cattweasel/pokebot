package net.cattweasel.pokebot.object;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "db_gym")
@XmlRootElement(name = "Gym")
public class Gym extends PokeObject {
	
	private static final long serialVersionUID = 7281718792637457962L;
	
	private Long eid;
	private Integer teamId;
	private Double latitude;
	private Double longitude;
	private Integer score;
	private Boolean inBatte;
	private Long timeOccupied;
	private Date raidStart;
	private Date raidEnd;
	private Integer raidLevel;
	private Integer raidPokemonId;
	private Integer raidCp;
	
	@Column(unique = false, nullable = false)
	@XmlAttribute
	public Long getEid() {
		return eid;
	}
	
	public void setEid(Long eid) {
		this.eid = eid;
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Integer getTeamId() {
		return teamId;
	}

	public void setTeamId(Integer teamId) {
		this.teamId = teamId;
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
	public Long getTimeOccupied() {
		return timeOccupied;
	}

	public void setTimeOccupied(Long timeOccupied) {
		this.timeOccupied = timeOccupied;
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Date getRaidStart() {
		return raidStart;
	}

	public void setRaidStart(Date raidStart) {
		this.raidStart = raidStart;
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Date getRaidEnd() {
		return raidEnd;
	}

	public void setRaidEnd(Date raidEnd) {
		this.raidEnd = raidEnd;
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Integer getRaidLevel() {
		return raidLevel;
	}

	public void setRaidLevel(Integer raidLevel) {
		this.raidLevel = raidLevel;
	}

	@Column(unique = false, nullable = true)
	@XmlAttribute
	public Integer getRaidPokemonId() {
		return raidPokemonId;
	}

	public void setRaidPokemonId(Integer raidPokemonId) {
		this.raidPokemonId = raidPokemonId;
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
