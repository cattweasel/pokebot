package net.cattweasel.pokebot.object;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import net.cattweasel.pokebot.tools.xml.AbstractXmlObject;

@XmlRootElement(name = "PokemonSetting")
public class PokemonSetting  extends AbstractXmlObject {
	
	private Integer pokemonId;
	private String pokemonName;
	private Boolean enabled;
	private Integer range;
	
	@XmlAttribute
	public Integer getPokemonId() {
		return pokemonId;
	}
	
	public void setPokemonId(Integer pokemonId) {
		this.pokemonId = pokemonId;
	}

	@XmlAttribute
	public String getPokemonName() {
		return pokemonName;
	}

	public void setPokemonName(String pokemonName) {
		this.pokemonName = pokemonName;
	}

	@XmlAttribute
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	@XmlAttribute
	public Integer getRange() {
		return range;
	}

	public void setRange(Integer range) {
		this.range = range;
	}
	
	@XmlAttribute
	public String getMessageKey() {
		return String.format("pokemon_%s", getPokemonId());
	}
}
