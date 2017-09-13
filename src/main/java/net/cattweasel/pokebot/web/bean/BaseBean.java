package net.cattweasel.pokebot.web.bean;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.OnetimeLink;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class BaseBean {

	private static final String ARG_ID = "id";
	private static final String ARG_USER = "user";
	
	private PokeContext context;
	private User user;
	
	public String getUserCount() throws GeneralException {
		return Util.separateNumber(getContext().countObjects(User.class));
	}
	
	public String getSessionCount() throws GeneralException {
		return Util.separateNumber(getContext().countObjects(BotSession.class));
	}
	
	public String getNotificationCount() throws GeneralException {
		return Util.separateNumber(getContext().countObjects(UserNotification.class));
	}
	
	public String getGymCount() throws GeneralException {
		return Util.separateNumber(getContext().countObjects(Gym.class));
	}
	
	public String getRaidCount() throws GeneralException {
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.notnull(ExtendedAttributes.GYM_RAID_POKEMON));
		return Util.separateNumber(getContext().countObjects(Gym.class, qo));
	}
	
	public User getLoggedInUser() throws GeneralException {
		if (user == null) {
			String userId = getRequestScope().get(ARG_ID);
			if (Util.isNotNullOrEmpty(userId)) {
				userId = resolveOnetimeLink(userId);
			} else {
				userId = Util.otos(getSessionScope().get(ARG_USER));
			}
			if (Util.isNotNullOrEmpty(userId)) {
				user = getContext().getObjectById(User.class, userId);
				if (user != null) {
					getSessionScope().put(ARG_USER, user.getId());
				}
			}
		}
		return user;
	}
	
	public String getLoggedInUserId() throws GeneralException {
		return getLoggedInUser() == null ? null : getLoggedInUser().getId();
	}
	
	public void logout() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
		if (session != null) {
			session.invalidate();
			if (getSessionScope() != null) {
				getSessionScope().put(ARG_USER, null);
			}
		}
	}
	
	protected PokeContext getContext() throws GeneralException {
		if (context == null) {
			context = PokeFactory.getCurrentContext();
		}
		return context;
	}
	
	protected String getRequestContextPath() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
	}
	
	protected Map<String, String> getRequestScope() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
	}
	
	protected Map<String, Object> getSessionScope() {
		return FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
	}
	
	private String resolveOnetimeLink(String userId) throws GeneralException {
		OnetimeLink link = getContext().getObjectByName(OnetimeLink.class, userId);
		return link == null || link.getUser() == null ? userId : link.getUser().getId();
	}
}
