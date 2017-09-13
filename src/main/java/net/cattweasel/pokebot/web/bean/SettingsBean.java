package net.cattweasel.pokebot.web.bean;

import javax.faces.context.FacesContext;

import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class SettingsBean extends BaseBean {

	private String language;
	private Boolean deleteExpired;
	private Boolean gymEnabled;
	private Integer gymLevel;
	private Integer gymRange;
	private Boolean updatesEnabled;
	
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
	
	public Boolean getUpdatesEnabled() throws GeneralException {
		updatesEnabled = getSetting(ExtendedAttributes.USER_SETTINGS_UPDATES_ENABLED) != null
				? Util.otob(getSetting(ExtendedAttributes.USER_SETTINGS_UPDATES_ENABLED)) : true;
		return updatesEnabled;
	}
	
	public void setUpdatesEnabled(Boolean updatesEnabled) {
		this.updatesEnabled = updatesEnabled;
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
	
	private Object getSetting(String key) throws GeneralException {
		Attributes<String, Object> attrs = getLoggedInUser() == null
				? null : getLoggedInUser().getSettings();
		Object result = attrs != null ? attrs.get(key) : null;
		return result;
	}
}
