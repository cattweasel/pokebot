package net.cattweasel.pokebot.web.bean;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class AbstractBean {

	private PokeContext context;
	
	private static final Logger LOG = Logger.getLogger(AbstractBean.class);
	
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
			context = PokeFactory.createContext(getClass().getSimpleName());
		}
		return context;
	}
	
	protected Map<String, String> getRequestScope() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
	}
}
