package net.cattweasel.pokebot.web.bean;

import javax.faces.bean.RequestScoped;
import javax.inject.Named;

import net.cattweasel.pokebot.object.User;

@Named
@RequestScoped
public class SettingsBean extends AbstractBean {

	private String userId;
	
	public String getUserId() {
		if (userId == null) {
			User user = getLoggedInUser();
			userId = user == null ? null : user.getId();
		}
		return userId;
	}
}
