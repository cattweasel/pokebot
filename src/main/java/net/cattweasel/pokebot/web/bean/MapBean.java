package net.cattweasel.pokebot.web.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.Spawn;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class MapBean extends BaseBean {

	public static class Player {
		
		private User user;
		private Double latitude;
		private Double longitude;
		
		public User getUser() {
			return user;
		}
		
		public void setUser(User user) {
			this.user = user;
		}

		public Double getLatitude() {
			return latitude;
		}

		public void setLatitude(Double latitude) {
			this.latitude = latitude;
		}

		public Double getLongitude() {
			return longitude;
		}

		public void setLongitude(Double longitude) {
			this.longitude = longitude;
		}
	}
	
	public static class Location {
		
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
	
	public List<Spawn> getUserSpawns() throws GeneralException {
		List<Spawn> spawns = new ArrayList<Spawn>();
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.like(ExtendedAttributes.POKE_OBJECT_NAME, String.format("%s:%s:",
				getLoggedInUser().getName(), Spawn.class.getSimpleName()), Filter.MatchMode.START));
		Iterator<String> it = getContext().search(UserNotification.class, qo);
		if (it != null) {
			while (it.hasNext()) {
				UserNotification notif = getContext().getObjectById(UserNotification.class, it.next());
				Spawn spawn = getContext().getObjectByName(Spawn.class, notif.getName().split(":")[2]);
				if (spawn != null) {
					spawns.add(spawn);
				}
			}
		}
		return spawns;
	}
	
	public List<Gym> getUserRaids() throws GeneralException {
		List<Gym> raids = new ArrayList<Gym>();
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.like(ExtendedAttributes.POKE_OBJECT_NAME, String.format("%s:%s:",
				getLoggedInUser().getName(), Gym.class.getSimpleName()), Filter.MatchMode.START));
		Iterator<String> it = getContext().search(UserNotification.class, qo);
		if (it != null) {
			while (it.hasNext()) {
				UserNotification notif = getContext().getObjectById(UserNotification.class, it.next());
				Gym gym = getContext().getObjectByName(Gym.class, notif.getName().split(":")[2]);
				if (gym != null) {
					raids.add(gym);
				}
			}
		}
		return raids;
	}
	
	public List<Player> getPlayers() throws GeneralException {
		List<Player> players = new ArrayList<Player>();
		for (BotSession session : getContext().getObjects(BotSession.class)) {
			if (session.getUser() != null && session.getUser().getSettings() != null
					&& session.getUser().getSettings().getBoolean(ExtendedAttributes.USER_SETTINGS_SHARE_LOCATION)) {
				Player player = new Player();
				player.setLatitude(Util.atod(Util.otos(session.getAttributes().get(ExtendedAttributes.BOT_SESSION_LATITUDE))));
				player.setLongitude(Util.atod(Util.otos(session.getAttributes().get(ExtendedAttributes.BOT_SESSION_LONGITUDE))));
				player.setUser(getLoggedInUser());
				players.add(player);
			}
		}
		return players;
	}
	
	public Location getUserPosition() throws GeneralException {
		Location loc = null;
		BotSession session = getContext().getUniqueObject(BotSession.class,
				Filter.eq(ExtendedAttributes.BOT_SESSION_USER, getLoggedInUser()));
		if (session != null && session.getAttributes() != null
				&& session.getAttributes().get(ExtendedAttributes.BOT_SESSION_LATITUDE) != null) {
			loc = new Location();
			loc.setX(Util.atod(Util.otos(session.getAttributes().get(ExtendedAttributes.BOT_SESSION_LATITUDE))));
			loc.setY(Util.atod(Util.otos(session.getAttributes().get(ExtendedAttributes.BOT_SESSION_LONGITUDE))));
		}
		return loc;
	}
	
	public boolean getSessionActive() throws GeneralException {
		BotSession session = getContext().getUniqueObject(BotSession.class,
				Filter.eq(ExtendedAttributes.BOT_SESSION_USER, getLoggedInUser()));
		return session != null;
	}
	
	public boolean getLocationAvailable() throws GeneralException {
		BotSession session = getContext().getUniqueObject(BotSession.class,
				Filter.eq(ExtendedAttributes.BOT_SESSION_USER, getLoggedInUser()));
		return session != null && session.getAttributes() != null
				&& session.getAttributes().get(ExtendedAttributes.BOT_SESSION_LATITUDE) != null;
	}
	
	public List<Location> getUserLocations() throws GeneralException {
		List<Location> result = new ArrayList<Location>();
		Iterator<String> it = getContext().search(BotSession.class);
		if (it != null) {
			while (it.hasNext()) {
				BotSession session = getContext().getObjectById(BotSession.class, it.next());
				if (session.get(ExtendedAttributes.BOT_SESSION_LATITUDE) != null
						&& session.get(ExtendedAttributes.BOT_SESSION_LONGITUDE) != null) {
					Location l = new Location();
					l.setX(Util.atod(Util.otos(session.get(ExtendedAttributes.BOT_SESSION_LATITUDE))));
					l.setY(Util.atod(Util.otos(session.get(ExtendedAttributes.BOT_SESSION_LONGITUDE))));
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
				Filter.like(ExtendedAttributes.POKE_OBJECT_NAME,String.format("%s:%s",
						session.getUser().getName(), Spawn.class.getSimpleName())),
				Filter.like(ExtendedAttributes.POKE_OBJECT_NAME, String.format("%s:%s",
						session.getUser().getName(), Gym.class.getSimpleName()))));
		return getContext().countObjects(UserNotification.class, qo);
	}
}
