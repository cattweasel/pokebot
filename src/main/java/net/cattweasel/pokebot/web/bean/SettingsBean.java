package net.cattweasel.pokebot.web.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.RequestScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Pokemon;
import net.cattweasel.pokebot.object.PokemonSetting;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

@Named
@RequestScoped
public class SettingsBean extends AbstractBean {

	private User user;
	private List<PokemonSetting> pokemonSettings;
	private Boolean gymEnabled;
	private Integer gymLevel;
	private Integer gymRange;
	
	private static final Logger LOG = Logger.getLogger(SettingsBean.class);
	
	public User getUser() {
		if (user == null) {
			user = getLoggedInUser();
		}
		return user;
	}
	
	public List<PokemonSetting> getPokemonSettings() {
		if (pokemonSettings == null) {
			pokemonSettings = createPokemonSettings();
		}
		return pokemonSettings;
	}
	
	public Boolean getGymEnabled() {
		if (gymEnabled == null) {
			gymEnabled = getSetting("gymEnabled") != null
					? Util.otob(getSetting("gymEnabled")) : true;
		}
		return gymEnabled;
	}
	
	public void setGymEnabled(Boolean gymEnabled) {
		this.gymEnabled = gymEnabled;
	}
	
	public Integer getGymLevel() {
		if (gymLevel == null) {
			gymLevel = getSetting("gymLevel") != null
					? Util.otoi(getSetting("gymLevel")) : 4;
		}
		return gymLevel;
	}
	
	public void setGymLevel(Integer gymLevel) {
		this.gymLevel = gymLevel;
	}
	
	public Integer getGymRange() {
		if (gymRange == null) {
			gymRange = getSetting("gymRange") != null
					? Util.otoi(getSetting("gymRange")) : 3000;
		}
		return gymRange;
	}
	
	public void setGymRange(Integer gymRange) {
		this.gymRange = gymRange;
	}
	
	public String save() {
		
		System.out.println("** SAVE **"); // TODO
		
		return "foobar";
		
	}
	
	private List<PokemonSetting> createPokemonSettings() {
		List<PokemonSetting> settings = new ArrayList<PokemonSetting>();
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.eq("enabled", true));
		qo.setOrder("pokemonId", "ASC");
		Iterator<String> it = null;
		try {
			it = getContext().search(Pokemon.class, qo);
			if (it != null);
			while (it.hasNext()) {
				Pokemon pokemon = getContext().getObjectById(Pokemon.class, it.next());
				settings.add(createPokemonSetting(pokemon));
			}
		} catch (GeneralException ex) {
			LOG.error(ex);
		}
		return settings;
	}
	
	private PokemonSetting createPokemonSetting(Pokemon pokemon) {
		PokemonSetting setting = new PokemonSetting();
		setting.setEnabled(isEnabled(pokemon));
		setting.setPokemonId(pokemon.getPokemonId());
		setting.setPokemonName(pokemon.getName());
		setting.setRange(getRange(pokemon));
		return setting;
	}
	
	private Boolean isEnabled(Pokemon pokemon) {
		return Util.otob(getSetting(String.format("%s-enabled", pokemon.getId())));
	}
	
	private Integer getRange(Pokemon pokemon) {
		int range = Util.otoi(getSetting(String.format("%s-range", pokemon.getId())));
		if (range == 0) {
			range = 3000;
		}
		return range;
	}
	
	private Object getSetting(String key) {
		Attributes<String, Object> attrs = user.getSettings();
		return attrs == null ? null : attrs.get(key);
	}
}
