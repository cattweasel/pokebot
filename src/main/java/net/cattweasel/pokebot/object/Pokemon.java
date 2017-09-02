package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "db_pokemon")
@XmlRootElement(name = "Pokemon")
public class Pokemon extends PokeObject {

	private static final long serialVersionUID = 7080727075450830557L;
	
	private Integer pokemonId;
	private Boolean enabled;
	
	@Column(unique = true, nullable = false)
	@XmlAttribute
	public Integer getPokemonId() {
		return pokemonId;
	}
	
	public void setPokemonId(Integer pokemonId) {
		this.pokemonId = pokemonId;
	}
	
	@Column(unique = false, nullable = false)
	@XmlAttribute
	public Boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
}
