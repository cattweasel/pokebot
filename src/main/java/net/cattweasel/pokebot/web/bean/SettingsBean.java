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
import net.cattweasel.pokebot.object.Profile;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class SettingsBean extends BaseBean {

	private static final String VIEW_ALL = "all";
	private static final String VIEW_SELECTED = "selected";
	private static final String VIEW_UNSELECTED = "unselected";
	
	private List<PokemonSetting> pokemonSettings;
	private String language;
	private Boolean deleteExpired;
	private Boolean gymEnabled;
	private Integer gymLevel;
	private Integer gymRange;
	private String profileName;
	private String selectedLoadProfile;
	private String selectedSaveProfile;
	private String selectedDeleteProfile;
	private Profile loadedProfile;
	private String view = VIEW_ALL;
	
	private static final Logger LOG = Logger.getLogger(SettingsBean.class);
	
	public String getProfileName() {
		return profileName;
	}
	
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
	public String getSelectedLoadProfile() {
		return selectedLoadProfile;
	}
	
	public void setSelectedLoadProfile(String selectedLoadProfile) {
		this.selectedLoadProfile = selectedLoadProfile;
	}
	
	public String getSelectedSaveProfile() {
		return selectedSaveProfile;
	}
	
	public void setSelectedSaveProfile(String selectedSaveProfile) {
		this.selectedSaveProfile = selectedSaveProfile;
	}
	
	public String getSelectedDeleteProfile() {
		return selectedDeleteProfile;
	}
	
	public void setSelectedDeleteProfile(String selectedDeleteProfile) {
		this.selectedDeleteProfile = selectedDeleteProfile;
	}
	
	public List<Profile> getAvailableProfiles() throws GeneralException {
		List<Profile> profiles = new ArrayList<Profile>();
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.eq(ExtendedAttributes.PROFILE_USER, getLoggedInUser()));
		qo.setOrder(ExtendedAttributes.PROFILE_DISPLAY_NAME, "ASC");
		Iterator<String> it = getContext().search(Profile.class, qo);
		if (it != null) {
			while (it.hasNext()) {
				profiles.add(getContext().getObjectById(Profile.class, it.next()));
			}
		}
		return profiles;
	}
	
	public void loadProfile() throws GeneralException {
		if (Util.isNotNullOrEmpty(selectedLoadProfile)) {
			loadedProfile = getContext().getObjectById(Profile.class, selectedLoadProfile);
		}
	}
	
	public void saveProfile() throws GeneralException {
		if (Util.isNotNullOrEmpty(selectedSaveProfile)) {
			Profile profile = getContext().getObjectById(Profile.class, selectedSaveProfile);
			if (profile != null) {
				profile.setSettings(createSettings());
				getContext().saveObject(profile);
				getContext().commitTransaction();
			}
		}
	}
	
	public void createProfile() throws GeneralException {
		if (Util.isNotNullOrEmpty(profileName) && getContext().getUniqueObject(
				Profile.class, Filter.and(Filter.eq(ExtendedAttributes.PROFILE_USER, getLoggedInUser()),
						Filter.eq(ExtendedAttributes.PROFILE_DISPLAY_NAME, profileName))) == null) {
			Profile profile = new Profile();
			profile.setDisplayName(profileName);
			profile.setName(Util.uuid());
			profile.setUser(getLoggedInUser());
			getContext().saveObject(profile);
			getContext().commitTransaction();
		}
		profileName = null;
	}
	
	public void deleteProfile() throws GeneralException {
		if (Util.isNotNullOrEmpty(selectedDeleteProfile)) {
			Profile profile = getContext().getObjectById(Profile.class, selectedDeleteProfile);
			if (profile != null) {
				getContext().removeObject(profile);
				getContext().commitTransaction();
			}
		}
	}
	
	public void updateView() throws GeneralException {
	}
	
	public String getView() {
		return view;
	}
	
	public void setView(String view) {
		this.view = view;
	}
	
	public List<PokemonSetting> getPokemonSettings() {
		pokemonSettings = createPokemonSettings();
		return pokemonSettings;
	}
	
	public String getLanguage() throws GeneralException {
		language = getSetting(ExtendedAttributes.USER_SETTINGS_LANGUAGE) != null
				? Util.otos(getSetting(ExtendedAttributes.USER_SETTINGS_LANGUAGE))
				: FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public Boolean getGymEnabled() throws GeneralException {
		gymEnabled = getSetting(ExtendedAttributes.USER_SETTINGS_GYM_ENABLED) != null
				? Util.otob(getSetting(ExtendedAttributes.USER_SETTINGS_GYM_ENABLED)) : true;
		return gymEnabled;
	}
	
	public void setGymEnabled(Boolean gymEnabled) {
		this.gymEnabled = gymEnabled;
	}
	
	public Boolean getDeleteExpired() throws GeneralException {
		deleteExpired = getSetting(ExtendedAttributes.USER_SETTINGS_DELETE_EXPIRED) != null
				? Util.otob(getSetting(ExtendedAttributes.USER_SETTINGS_DELETE_EXPIRED)) : true;
		return deleteExpired;
	}
	
	public void setDeleteExpired(Boolean deleteExpired) {
		this.deleteExpired = deleteExpired;
	}
	
	public Integer getGymLevel() throws GeneralException {
		gymLevel = getSetting(ExtendedAttributes.USER_SETTINGS_GYM_LEVEL) != null
				? Util.otoi(getSetting(ExtendedAttributes.USER_SETTINGS_GYM_LEVEL)) : 4;
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
	
	public Integer getGymRange() throws GeneralException {
		gymRange = getSetting(ExtendedAttributes.USER_SETTINGS_GYM_RANGE) != null
				? Util.otoi(getSetting(ExtendedAttributes.USER_SETTINGS_GYM_RANGE)) : 3000;
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
	
	public void save() throws GeneralException {
		User user = getLoggedInUser();
		user.setSettings(createSettings());
		getContext().saveObject(user);
		Auditor auditor = new Auditor(getContext());
		auditor.log(user.getName(), AuditAction.UPDATE_SETTINGS, user.getName());
		getContext().commitTransaction();
	}
	
	private Attributes<String, Object> createSettings() throws GeneralException {
		Attributes<String, Object> settings = getLoggedInUser().getSettings();
		settings = settings == null ? new Attributes<String, Object>() : settings;
		settings.put(ExtendedAttributes.USER_SETTINGS_LANGUAGE, language);
		FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(language));
		settings.put(ExtendedAttributes.USER_SETTINGS_DELETE_EXPIRED, deleteExpired);
		settings.put(ExtendedAttributes.USER_SETTINGS_GYM_ENABLED, gymEnabled);
		settings.put(ExtendedAttributes.USER_SETTINGS_GYM_LEVEL, gymLevel);
		settings.put(ExtendedAttributes.USER_SETTINGS_GYM_RANGE, gymRange);
		for (PokemonSetting s : pokemonSettings) {
			if (s.getRange() != null && s.getRange() < 500) {
				s.setRange(500);
			}
			if (s.getRange() != null && s.getRange() > 50000) {
				s.setRange(50000);
			}
			settings.put(String.format("%s-enabled", s.getPokemonId()), s.getEnabled());
			settings.put(String.format("%s-range", s.getPokemonId()), s.getRange());
		}
		return settings;
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
					if (isPartOfView(pokemon)) {
						settings.add(createPokemonSetting(pokemon));
					}
				}
			}
		} catch (GeneralException ex) {
			LOG.error(ex);
		}
		return settings;
	}
	
	private boolean isPartOfView(Pokemon pokemon) throws GeneralException {
		boolean result = VIEW_ALL.equals(view);
		if (!result) {
			if (isEnabled(pokemon) && VIEW_SELECTED.equals(view)) {
				result = true;
			} else if (!isEnabled(pokemon) && VIEW_UNSELECTED.equals(view)) {
				result = true;
			}
		}
		return result;
	}
	
	private PokemonSetting createPokemonSetting(Pokemon pokemon) throws GeneralException {
		PokemonSetting setting = new PokemonSetting();
		setting.setEnabled(isEnabled(pokemon));
		setting.setPokemonId(pokemon.getPokemonId());
		setting.setPokemonName(pokemon.getName());
		setting.setRange(getRange(pokemon));
		return setting;
	}
	
	private Boolean isEnabled(Pokemon pokemon) throws GeneralException {
		return Util.otob(getSetting(String.format("%s-enabled", pokemon.getPokemonId())));
	}
	
	private Integer getRange(Pokemon pokemon) throws GeneralException {
		int range = Util.otoi(getSetting(String.format("%s-range", pokemon.getPokemonId())));
		if (range == 0) {
			range = 3000;
		}
		return range;
	}
	
	private Object getSetting(String key) throws GeneralException {
		Attributes<String, Object> attrs = getLoggedInUser() == null
				? null : getLoggedInUser().getSettings();
		Object result = loadedProfile != null && loadedProfile.getSettings() != null
				? loadedProfile.getSettings().get(key) : attrs != null ? attrs.get(key) : null;
		return result;
	}
}
