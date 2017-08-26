package net.cattweasel.pokebot.web.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Pokemon;
import net.cattweasel.pokebot.object.PokemonSetting;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class SettingsBean extends BaseBean {

	private List<PokemonSetting> pokemonSettings;
	private String language;
	private Boolean deleteExpired;
	private Boolean shareLocation;
	private Boolean gymEnabled;
	private Integer gymLevel;
	private Integer gymRange;
	private Boolean gymInvite;
	
	private static final Logger LOG = Logger.getLogger(SettingsBean.class);
	
	public List<PokemonSetting> getPokemonSettings() {
		if (pokemonSettings == null) {
			pokemonSettings = createPokemonSettings();
		}
		return pokemonSettings;
	}
	
	public String getLanguage() {
		language = getSetting(ExtendedAttributes.USER_SETTINGS_LANGUAGE) != null
				? Util.otos(getSetting(ExtendedAttributes.USER_SETTINGS_LANGUAGE))
				: FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public Boolean getGymEnabled() {
		if (gymEnabled == null) {
			gymEnabled = getSetting(ExtendedAttributes.USER_SETTINGS_GYM_ENABLED) != null
					? Util.otob(getSetting(ExtendedAttributes.USER_SETTINGS_GYM_ENABLED)) : true;
		}
		return gymEnabled;
	}
	
	public void setGymEnabled(Boolean gymEnabled) {
		this.gymEnabled = gymEnabled;
	}
	
	public Boolean getDeleteExpired() {
		if (deleteExpired == null) {
			deleteExpired = getSetting(ExtendedAttributes.USER_SETTINGS_DELETE_EXPIRED) != null
					? Util.otob(getSetting(ExtendedAttributes.USER_SETTINGS_DELETE_EXPIRED)) : true;
		}
		return deleteExpired;
	}
	
	public void setDeleteExpired(Boolean deleteExpired) {
		this.deleteExpired = deleteExpired;
	}
	
	public Boolean getShareLocation() {
		if (shareLocation == null) {
			shareLocation = getSetting(ExtendedAttributes.USER_SETTINGS_SHARE_LOCATION) != null
					? Util.otob(getSetting(ExtendedAttributes.USER_SETTINGS_SHARE_LOCATION)) : false;
		}
		return shareLocation;
	}
	
	public void setShareLocation(Boolean shareLocation) {
		this.shareLocation = shareLocation;
	}
	
	public Integer getGymLevel() {
		if (gymLevel == null) {
			gymLevel = getSetting(ExtendedAttributes.USER_SETTINGS_GYM_LEVEL) != null
					? Util.otoi(getSetting(ExtendedAttributes.USER_SETTINGS_GYM_LEVEL)) : 4;
		}
		return gymLevel;
	}
	
	public void setGymLevel(Integer gymLevel) {
		if (gymLevel != null && gymLevel < 1) {
			gymLevel = 1;
		}
		if (gymLevel != null && gymLevel > 5) {
			gymLevel = 5;
		}
		this.gymLevel = gymLevel;
	}
	
	public Integer getGymRange() {
		if (gymRange == null) {
			gymRange = getSetting(ExtendedAttributes.USER_SETTINGS_GYM_RANGE) != null
					? Util.otoi(getSetting(ExtendedAttributes.USER_SETTINGS_GYM_RANGE)) : 3000;
		}
		return gymRange;
	}
	
	public void setGymRange(Integer gymRange) {
		if (gymRange != null && gymRange < 500) {
			gymRange = 500;
		}
		if (gymRange != null && gymRange > 20000) {
			gymRange = 20000;
		}
		this.gymRange = gymRange;
	}
	
	public Boolean getGymInvite() {
		if (gymInvite == null) {
			gymInvite = getSetting(ExtendedAttributes.USER_SETTINGS_GYM_INVITE) != null
					? Util.otob(getSetting(ExtendedAttributes.USER_SETTINGS_GYM_INVITE)) : true;
		}
		return gymInvite;
	}
	
	public void setGymInvite(Boolean gymInvite) {
		this.gymInvite = gymInvite;
	}
	
	public void save() throws GeneralException {
		User user = getLoggedInUser();
		Attributes<String, Object> settings = user.getSettings();
		settings = settings == null ? new Attributes<String, Object>() : settings;
		settings.put(ExtendedAttributes.USER_SETTINGS_LANGUAGE, language);
		FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(language));
		settings.put(ExtendedAttributes.USER_SETTINGS_DELETE_EXPIRED, getDeleteExpired());
		settings.put(ExtendedAttributes.USER_SETTINGS_GYM_ENABLED, getGymEnabled());
		settings.put(ExtendedAttributes.USER_SETTINGS_GYM_LEVEL, getGymLevel());
		settings.put(ExtendedAttributes.USER_SETTINGS_GYM_RANGE, getGymRange());
		settings.put(ExtendedAttributes.USER_SETTINGS_GYM_INVITE, gymInvite);
		settings.put(ExtendedAttributes.USER_SETTINGS_SHARE_LOCATION, shareLocation);
		for (PokemonSetting s : getPokemonSettings()) {
			if (s.getRange() != null && s.getRange() < 500) {
				s.setRange(500);
			}
			if (s.getRange() != null && s.getRange() > 20000) {
				s.setRange(20000);
			}
			settings.put(String.format("%s-enabled", s.getPokemonId()), s.getEnabled());
			settings.put(String.format("%s-range", s.getPokemonId()), s.getRange());
		}
		user.setSettings(settings);
		getContext().saveObject(user);
		Auditor auditor = new Auditor(getContext());
		auditor.log(user.getName(), AuditAction.UPDATE_SETTINGS, user.getName());
		getContext().commitTransaction();
	}
	
	private List<PokemonSetting> createPokemonSettings() {
		List<PokemonSetting> settings = new ArrayList<PokemonSetting>();
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.eq(ExtendedAttributes.POKEMON_ENABLED, true));
		qo.setOrder(ExtendedAttributes.POKEMON_POKEMON_ID, "ASC");
		Iterator<String> it = null;
		try {
			it = getContext().search(Pokemon.class, qo);
			if (it != null) {
				while (it.hasNext()) {
					Pokemon pokemon = getContext().getObjectById(Pokemon.class, it.next());
					settings.add(createPokemonSetting(pokemon));
				}
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
		return Util.otob(getSetting(String.format("%s-enabled", pokemon.getPokemonId())));
	}
	
	private Integer getRange(Pokemon pokemon) {
		int range = Util.otoi(getSetting(String.format("%s-range", pokemon.getPokemonId())));
		if (range == 0) {
			range = 3000;
		}
		return range;
	}
	
	private Object getSetting(String key) {
		Attributes<String, Object> attrs = getLoggedInUser() == null
				? null : getLoggedInUser().getSettings();
		return attrs == null ? null : attrs.get(key);
	}
}
