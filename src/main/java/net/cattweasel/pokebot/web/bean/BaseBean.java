package net.cattweasel.pokebot.web.bean;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.Spawn;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class BaseBean {

	private PokeContext context;
	
	private static final Logger LOG = Logger.getLogger(BaseBean.class);
	
	public Integer getUserCount() throws GeneralException {
		return getContext().countObjects(User.class);
	}
	
	public Integer getSessionCount() throws GeneralException {
		return getContext().countObjects(BotSession.class);
	}
	
	public Integer getNotificationCount() throws GeneralException {
		return getContext().countObjects(UserNotification.class);
	}
	
	public Integer getGymCount() throws GeneralException {
		return getContext().countObjects(Gym.class);
	}
	
	public Integer getRaidCount() throws GeneralException {
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.notnull("raidPokemon"));
		return getContext().countObjects(Gym.class, qo);
	}
	
	public Integer getSpawnCount() throws GeneralException {
		return getContext().countObjects(Spawn.class);
	}
	
	protected User getLoggedInUser() {
		User user = null;
		String userId = getRequestScope().get("id");
		if (Util.isNotNullOrEmpty(userId)) {
			try {
				user = getContext().getObjectById(User.class, userId);
			} catch (GeneralException ex) {
				LOG.error(ex);
			}
		}
		return user;
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
