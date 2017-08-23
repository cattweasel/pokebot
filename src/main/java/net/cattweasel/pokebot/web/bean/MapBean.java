package net.cattweasel.pokebot.web.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.Spawn;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class MapBean extends BaseBean {

	public class Location {
		
		private Double x;
		private Double y;
		private Integer notifications;
		
		public Double getX() {
			return x;
		}
		
		public void setX(Double x) {
			this.x = x;
		}

		public Double getY() {
			return y;
		}

		public void setY(Double y) {
			this.y = y;
		}

		public Integer getNotifications() {
			return notifications;
		}

		public void setNotifications(Integer notifications) {
			this.notifications = notifications;
		}
	}
	
	private static final String NAME = "name";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	
	public List<Location> getUserLocations() throws GeneralException {
		List<Location> result = new ArrayList<Location>();
		Iterator<String> it = getContext().search(BotSession.class);
		if (it != null) {
			while (it.hasNext()) {
				BotSession session = getContext().getObjectById(BotSession.class, it.next());
				if (session.get(LATITUDE) != null && session.get(LONGITUDE) != null) {
					Location l = new Location();
					l.setX(Util.atod(Util.otos(session.get(LATITUDE))));
					l.setY(Util.atod(Util.otos(session.get(LONGITUDE))));
					l.setNotifications(calculateNotifications(session));
					result.add(l);
				}
			}
		}
		return result;
	}

	private Integer calculateNotifications(BotSession session) throws GeneralException {
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.or(
				Filter.like(NAME, String.format("%s:%s", session.getUser().getName(), Spawn.class.getSimpleName())),
				Filter.like(NAME, String.format("%s:%s", session.getUser().getName(), Gym.class.getSimpleName()))));
		return getContext().countObjects(UserNotification.class, qo);
	}
}