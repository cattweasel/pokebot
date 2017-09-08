package net.cattweasel.pokebot.web.bean;

import java.util.ArrayList;
import java.util.Date;
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
import net.cattweasel.pokebot.tools.GeoLocation;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class MapBean extends BaseBean {

	public static class ReturnValue {
		
		private String icon;
		private Double latitude;
		private Double longitude;
		private String description;

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

		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
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
	
	public List<ReturnValue> getUserSpawns() throws GeneralException {
		List<ReturnValue> spawns = new ArrayList<ReturnValue>();
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.like(ExtendedAttributes.POKE_OBJECT_NAME, String.format("%s:%s:",
				getLoggedInUser().getName(), Spawn.class.getSimpleName()), Filter.MatchMode.START));
		Iterator<String> it = getContext().search(UserNotification.class, qo);
		if (it != null) {
			while (it.hasNext()) {
				UserNotification notif = getContext().getObjectById(UserNotification.class, it.next());
				Spawn spawn = getContext().getObjectByName(Spawn.class, notif.getName().split(":")[2]);
				if (spawn != null) {
					ReturnValue s = new ReturnValue();
					s.setLatitude(spawn.getLatitude());
					s.setLongitude(spawn.getLongitude());
					s.setIcon(getRequestContextPath() + "/img/pokemon/" + spawn.getPokemon().getPokemonId() + ".png");
					s.setDescription(formatSpawnDescription(spawn));
					spawns.add(s);
				}
			}
		}
		return spawns;
	}
	
	public List<ReturnValue> getUserRaids() throws GeneralException {
		List<ReturnValue> raids = new ArrayList<ReturnValue>();
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.like(ExtendedAttributes.POKE_OBJECT_NAME, String.format("%s:%s:",
				getLoggedInUser().getName(), Gym.class.getSimpleName()), Filter.MatchMode.START));
		Iterator<String> it = getContext().search(UserNotification.class, qo);
		if (it != null) {
			while (it.hasNext()) {
				UserNotification notif = getContext().getObjectById(UserNotification.class, it.next());
				Gym gym = getContext().getObjectByName(Gym.class, notif.getName().split(":")[2]);
				if (gym != null) {
					ReturnValue raid = new ReturnValue();
					raid.setLatitude(gym.getLatitude());
					raid.setLongitude(gym.getLongitude());
					if (gym.getTeam() != null) {
						raid.setIcon(getRequestContextPath() + "/img/teams/" + gym.getTeam().getTeamId() + ".png");
					} else {
						raid.setIcon(getRequestContextPath() + "/img/icons/gym.png");
					}
					raid.setDescription(formatRaidDescription(gym));
					raids.add(raid);
				}
			}
		}
		return raids;
	}
	
	private String formatRaidDescription(Gym gym) throws GeneralException {
		User user = getLoggedInUser();
		BotSession session = getContext().getUniqueObject(BotSession.class,
				Filter.eq(ExtendedAttributes.BOT_SESSION_USER, user));
		String pokemon = gym.getRaidPokemon() == null ? "n/a" : Localizer.localize(user,
				String.format("pokemon_%s", gym.getRaidPokemon().getPokemonId()));
		return String.format("Pokemon: %s (Level %s)<br/>%s: %s<br/>Team: %s<br/>%s: %sm<br/>%s: %s (%sm)", pokemon,
				gym.getRaidLevel(), Localizer.localize(user, "gym"), gym.getDisplayName(),
				gym.getTeam() == null ? "n/a" : Localizer.localize(user, String.format("team_%s",
						gym.getTeam().getTeamId())), Localizer.localize(user, "distance"),
				Util.separateNumber(Math.round(calculateDistance(session, gym.getLatitude(), gym.getLongitude()))),
				Localizer.localize(user, "end"), Localizer.localize(user, gym.getRaidEnd()),
				(gym.getRaidEnd().getTime() - new Date().getTime()) / 60000);
	}
	
	private String formatUserDescription(BotSession session) throws GeneralException {
		User user = getLoggedInUser();
		return String.format("%s: %s<br/>%s", Localizer.localize(user, "user"),
				Util.isNullOrEmpty(session.getUser().getUsername()) ? session.getUser().getName()
						: session.getUser().getUsername(), Localizer.localize(user, session.getModified() == null
						? session.getCreated() : session.getModified(),  true));
	}
	
	private String formatSpawnDescription(Spawn spawn) throws GeneralException {
		User user = getLoggedInUser();
		BotSession session = getContext().getUniqueObject(BotSession.class, Filter.eq(ExtendedAttributes.BOT_SESSION_USER, user));
		return String.format("Pokemon: %s<br/>%s: %sm<br/>%s: %s (%sm)", Localizer.localize(user, String.format("pokemon_%s",
				spawn.getPokemon().getPokemonId())), Localizer.localize(user, "distance"),
				Util.separateNumber(Math.round(calculateDistance(session, spawn.getLatitude(), spawn.getLongitude()))),
				Localizer.localize(user, "end"), Localizer.localize(user, spawn.getDisappearTime()),
				(spawn.getDisappearTime().getTime() - new Date().getTime()) / 60000);
	}
	
	private Double calculateDistance(BotSession session, Double lat, Double lng) {
		Double distance = 0D;
		if (session != null && session.get(ExtendedAttributes.BOT_SESSION_LATITUDE) != null
				&& session.get(ExtendedAttributes.BOT_SESSION_LONGITUDE) != null) {
			GeoLocation loc = GeoLocation.fromDegrees(lat, lng);
			Double la = Util.atod(Util.otos(session.get(ExtendedAttributes.BOT_SESSION_LATITUDE)));
			Double lo = Util.atod(Util.otos(session.get(ExtendedAttributes.BOT_SESSION_LONGITUDE)));
			distance = loc.distanceTo(GeoLocation.fromDegrees(la, lo));
		}
		return distance;
	}

	public List<ReturnValue> getPlayers() throws GeneralException {
		List<ReturnValue> players = new ArrayList<ReturnValue>();
		BotSession own = getContext().getUniqueObject(BotSession.class,
				Filter.eq(ExtendedAttributes.BOT_SESSION_USER, getLoggedInUser()));
		if (own != null) {
			ReturnValue p = new ReturnValue();
			p.setLatitude(Util.atod(Util.otos(own.getAttributes().get(ExtendedAttributes.BOT_SESSION_LATITUDE))));
			p.setLongitude(Util.atod(Util.otos(own.getAttributes().get(ExtendedAttributes.BOT_SESSION_LONGITUDE))));
			p.setIcon(getRequestContextPath() + "/img/icons/player.png");
			p.setDescription(formatUserDescription(own));
			players.add(p);
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
