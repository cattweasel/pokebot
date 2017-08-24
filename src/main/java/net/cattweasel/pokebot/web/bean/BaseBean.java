package net.cattweasel.pokebot.web.bean;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class BaseBean {

	private PokeContext context;
	private User user;
	
	private static final Logger LOG = Logger.getLogger(BaseBean.class);
	
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
	
	public User getLoggedInUser() {
		if (user == null) {
			String userId = getRequestScope().get("id");
			if (Util.isNotNullOrEmpty(userId)) {
				try {
					user = getContext().getObjectById(User.class, userId);
				} catch (GeneralException ex) {
					LOG.error(ex);
				}
			}
		}
		return user;
	}
	
	public void logout() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
	
	protected PokeContext getContext() throws GeneralException {
		if (context == null) {
			context = PokeFactory.getCurrentContext();
		}
		return context;
	}
	
	protected Map<String, String> getRequestScope() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
	}
}
