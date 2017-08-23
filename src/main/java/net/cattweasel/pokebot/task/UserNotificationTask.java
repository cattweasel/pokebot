package net.cattweasel.pokebot.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.api.TelegramBot;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.Pokemon;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.Spawn;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.server.Environment;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.GeoLocation;
import net.cattweasel.pokebot.tools.GeoLocation.BoundingCoordinates;
import net.cattweasel.pokebot.tools.Util;

/**
 * This task is used to send out notifications to users.
 * 
 * @author Benjamin Wesp
 *
 */
public class UserNotificationTask implements TaskExecutor {
	
	private static final String ARG_LATITUDE = "latitude";
	private static final String ARG_LONGITUDE = "longitude";
	private static final String ARG_RAID_LEVEL = "raidLevel";
	private static final String ARG_RAID_END = "raidEnd";
	private static final String ARG_GYM_RANGE = "gymRange";
	private static final String ARG_GYM_LEVEL = "gymLevel";
	private static final String ARG_GYM_ENABLED = "gymEnabled";
	
	private boolean running = true;
	
	private static final Logger LOG = Logger.getLogger(UserNotificationTask.class);
	
	@Override
	public void execute(PokeContext context, TaskSchedule schedule, TaskResult result,
			Attributes<String, Object> attributes) throws Exception {
		Iterator<String> it = context.search(BotSession.class);
		if (it != null) {
			while (running && it.hasNext()) {
				BotSession session = context.getObjectById(BotSession.class, it.next());
				handleSession(context, session);
			}
		}
	}

	@Override
	public boolean terminate() {
		running = false; 
		return true;
	}
	
	private void handleSession(PokeContext context, BotSession session) {
		Double lat = Util.atod(Util.otos(session.get(ARG_LATITUDE)));
		Double lon = Util.atod(Util.otos(session.get(ARG_LONGITUDE)));
		if (lat != null && lat != 0.0D && lon != null && lon != 0.0D) {
			handleSession(context, session, GeoLocation.fromDegrees(lat, lon));
		}
	}
	
	private void handleSession(PokeContext context, BotSession session, GeoLocation loc) {
		if (session.getUser().getSettings() == null
				|| session.getUser().getSettings().get(ARG_GYM_ENABLED) == null
				|| Util.otob(session.getUser().getSettings().get(ARG_GYM_ENABLED))) {
			handleGyms(context, session, loc);
		}
		handleSpawns(context, session, loc);
	}
	
	private void handleGyms(PokeContext context, BotSession session, GeoLocation loc) {
		Double range = session.getUser().getSettings() == null
				? null : Util.atod(Util.otos(session.getUser().getSettings().get(ARG_GYM_RANGE)));
		range = range == null || range == 0D ? 3000D : range;
		Integer minLevel = session.getUser().getSettings() == null
				? null : Util.otoi(session.getUser().getSettings().get(ARG_GYM_LEVEL));
		minLevel = minLevel == null || minLevel == 0 ? 4 : minLevel;
		QueryOptions qo = new QueryOptions();
		BoundingCoordinates coords = loc.boundingCoordinates(range);
		qo.addFilter(Filter.gt(ARG_RAID_LEVEL, (minLevel - 1)));
		qo.addFilter(Filter.gt(ARG_RAID_END, new Date(new Date().getTime() + 1800000L)));
		qo.addFilter(Filter.gt(ARG_LATITUDE, coords.getX().getLatitudeInDegrees()));
		qo.addFilter(Filter.gt(ARG_LONGITUDE, coords.getX().getLongitudeInDegrees()));
		qo.addFilter(Filter.lt(ARG_LATITUDE, coords.getY().getLatitudeInDegrees()));
		qo.addFilter(Filter.lt(ARG_LONGITUDE, coords.getY().getLongitudeInDegrees()));
		Iterator<String> it = null;
		try {
			it = context.search(Gym.class, qo);
			if (it != null) {
				while (it.hasNext()) {
					Gym gym = context.getObjectById(Gym.class, it.next());
					handleGym(context, session, gym);
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error handling Session: " + ex.getMessage(), ex);
		}
	}
	
	private void handleSpawns(PokeContext context, BotSession session, GeoLocation loc) {
		Attributes<String, Object> attrs = session.getUser().getSettings();
		if (attrs != null) {
			for (String key : attrs.keySet()) {
				if (key.endsWith("-enabled") && Util.otob(attrs.get(key))) {
					Integer pokemonId = Util.atoi(key.split("-")[0]);
					Double range = Util.atod(attrs.getString(String.format("%s-range", pokemonId)));
					handleSpawns(context, session, loc, pokemonId, range);
				}
			}
		}
	}
	
	private void handleSpawns(PokeContext context, BotSession session,
			GeoLocation loc, Integer pokemonId, Double distance) {
		try {
			Pokemon pokemon = context.getUniqueObject(Pokemon.class, Filter.eq("pokemonId", pokemonId));
			BoundingCoordinates coords = loc.boundingCoordinates(distance);
			QueryOptions qo = new QueryOptions();
			qo.addFilter(Filter.eq("pokemon", pokemon));
			qo.addFilter(Filter.gt("disappearTime", new Date()));
			qo.addFilter(Filter.gt(ARG_LATITUDE, coords.getX().getLatitudeInDegrees()));
			qo.addFilter(Filter.gt(ARG_LONGITUDE, coords.getX().getLongitudeInDegrees()));
			qo.addFilter(Filter.lt(ARG_LATITUDE, coords.getY().getLatitudeInDegrees()));
			qo.addFilter(Filter.lt(ARG_LONGITUDE, coords.getY().getLongitudeInDegrees()));
			Iterator<String> it = context.search(Spawn.class, qo);
			if (it != null) {
				while (it.hasNext()) {
					Spawn spawn = context.getObjectById(Spawn.class, it.next());
					handleSpawn(context, session, spawn);
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error handling Session: " + ex.getMessage(), ex);
		}
	}
	
	private void handleSpawn(PokeContext context, BotSession session, Spawn spawn) {
		String name = String.format("%s:%s:%s", session.getUser().getName(),
				spawn.getClass().getSimpleName(), spawn.getName());
		try {
			if (context.getObjectByName(UserNotification.class, name) == null) {
				String messageId = null;
				try {
					messageId = announceSpawn(session, spawn);
				} catch (TelegramApiException ex) {
					LOG.error("Error announcing Spawn: " + ex.getMessage(), ex);
				}
				if (messageId != null) {
					UserNotification n = new UserNotification();
					n.setName(name);
					n.setExpiration(spawn.getDisappearTime());
					n.setMessageId(messageId);
					context.saveObject(n);
					Auditor auditor = new Auditor(context);
					auditor.log("System", AuditAction.SEND_SPAWN_NOTIFICATION, session.getUser().getName());
					context.commitTransaction();
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error handling Spawn: " + ex.getMessage(), ex);
		}
	}
	
	@SuppressWarnings("deprecation")
	private String announceSpawn(BotSession session, Spawn spawn) throws TelegramApiException {
		String lang = session.getUser().getSettings() == null || session.getUser().getSettings().get("language") == null
				? "de" : session.getUser().getSettings().getString("language");
		TelegramBot bot = Environment.getEnvironment().getTelegramBot();
		SimpleDateFormat sdf = "en".equals(lang) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("HH:mm");
		String txt = String.format("PKM: %s - %s%s: %s%s", spawn.getPokemon().getName(),
				calculateDistance(session, spawn.getLatitude(), spawn.getLongitude()),
				"en".equals(lang) ? "End" : "Ende", sdf.format(spawn.getDisappearTime()),
				"en".equals(lang) ? "" : " Uhr");
		SendMessage msg = new SendMessage();
		msg.setChatId(session.getChatId());
		msg.setText(txt);
		Message m = bot.sendMessage(msg);
		String messageId = session.getChatId() + ":" + m.getMessageId();
		SendLocation loc = new SendLocation();
		loc.setChatId(session.getChatId());
		loc.setLatitude(Util.atof(Util.otos(spawn.getLatitude())));
		loc.setLongitude(Util.atof(Util.otos(spawn.getLongitude())));
		m = bot.sendLocation(loc);
		return messageId + "-" + session.getChatId() + ":" + m.getMessageId();
	}
	
	private void handleGym(PokeContext context, BotSession session, Gym gym) {
		String name = String.format("%s:%s:%s", session.getUser().getName(),
				gym.getClass().getSimpleName(), gym.getName());
		try {
			if (context.getObjectByName(UserNotification.class, name) == null) {
				String messageId = null;
				try {
					messageId = announceGym(session, gym);
				} catch (TelegramApiException ex) {
					LOG.error("Error announcing Gym: " + ex.getMessage(), ex);
				}
				if (messageId != null) {
					UserNotification n = new UserNotification();
					n.setName(name);
					n.setExpiration(gym.getRaidEnd());
					n.setMessageId(messageId);
					context.saveObject(n);
					Auditor auditor = new Auditor(context);
					auditor.log("System", AuditAction.SEND_RAID_NOTIFICATION, session.getUser().getName());
					context.commitTransaction();
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error handling Gym: " + ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("deprecation")
	private String announceGym(BotSession session, Gym gym) throws TelegramApiException {
		String lang = session.getUser().getSettings() == null || session.getUser().getSettings().get("language") == null
				? "de" : session.getUser().getSettings().getString("language");
		TelegramBot bot = Environment.getEnvironment().getTelegramBot();
		SimpleDateFormat sdf = "en".equals(lang) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("HH:mm");
		String txt = String.format("RAID: %s [ Level: %s, CP: %s, %s: %s ] %s%s: %s%s",
				gym.getRaidPokemon().getName(), gym.getRaidLevel(), Util.separateNumber(gym.getRaidCp()),
				"en".equals(lang) ? "Gym" : "Arena", gym.getDisplayName(),
				calculateDistance(session, gym.getLatitude(),gym.getLongitude()),
				"en".equals(lang) ? "End" : "Ende", sdf.format(gym.getRaidEnd()), "en".equals(lang) ? "" : " Uhr");
		SendMessage msg = new SendMessage();
		msg.setChatId(session.getChatId());
		msg.setText(txt);
		Message m = bot.sendMessage(msg);
		String messageId = session.getChatId() + ":" + m.getMessageId();
		SendLocation loc = new SendLocation();
		loc.setChatId(session.getChatId());
		loc.setLatitude(Util.atof(Util.otos(gym.getLatitude())));
		loc.setLongitude(Util.atof(Util.otos(gym.getLongitude())));
		m = bot.sendLocation(loc);
		return messageId + "-" + session.getChatId() + ":" + m.getMessageId();
	}
	
	private String calculateDistance(BotSession session, Double latitude, Double longitude) {
		StringBuilder sb = new StringBuilder();
		if (session != null && session.get(ARG_LATITUDE) != null && session.get(ARG_LONGITUDE) != null) {
			String lang = session.getUser().getSettings() == null || session.getUser().getSettings().get("language") == null
					? "de" : session.getUser().getSettings().getString("language");
			GeoLocation loc = GeoLocation.fromDegrees(latitude, longitude);
			Double lat = Util.atod(Util.otos(session.get(ARG_LATITUDE)));
			Double lon = Util.atod(Util.otos(session.get(ARG_LONGITUDE)));
			Double distance = loc.distanceTo(GeoLocation.fromDegrees(lat, lon));
			sb.append(String.format("%s: %sm - ", "en".equals(lang) ? "Distance" : "Entfernung",
					Util.separateNumber(Math.round(distance))));
		}
		return sb.toString();
	}
}
