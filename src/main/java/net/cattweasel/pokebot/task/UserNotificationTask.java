package net.cattweasel.pokebot.task;

import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.ExtendedAttributes;
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
import net.cattweasel.pokebot.server.TelegramBot;
import net.cattweasel.pokebot.tools.BotKeyboard;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.GeoLocation;
import net.cattweasel.pokebot.tools.GeoLocation.BoundingCoordinates;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

/**
 * This task is used to send out notifications to users.
 * 
 * @author Benjamin Wesp
 *
 */
public class UserNotificationTask implements TaskExecutor {
	
	private boolean running = true;
	
	private static final Logger LOG = Logger.getLogger(UserNotificationTask.class);
	
	@Override
	public void execute(PokeContext context, TaskSchedule schedule, TaskResult result,
			Attributes<String, Object> attributes) throws Exception {
		Iterator<String> it = context.search(BotSession.class);
		if (it != null) {
			while (running && it.hasNext()) {
				BotSession session = context.getObjectById(BotSession.class, it.next());
				if (session.getUser().isBanned() == null || session.getUser().isBanned() == false) {
					handleSession(context, session);
				}
			}
		}
	}

	@Override
	public boolean terminate() {
		running = false; 
		return true;
	}
	
	private void handleSession(PokeContext context, BotSession session) {
		Double lat = Util.atod(Util.otos(session.get(ExtendedAttributes.BOT_SESSION_LATITUDE)));
		Double lon = Util.atod(Util.otos(session.get(ExtendedAttributes.BOT_SESSION_LONGITUDE)));
		if (lat != 0.0D && lon != 0.0D) {
			handleSession(context, session, GeoLocation.fromDegrees(lat, lon));
		}
	}
	
	private void handleSession(PokeContext context, BotSession session, GeoLocation loc) {
		if (session.getUser().getSettings() == null
				|| session.getUser().getSettings().get(ExtendedAttributes.USER_SETTINGS_GYM_ENABLED) == null
				|| Util.otob(session.getUser().getSettings().get(ExtendedAttributes.USER_SETTINGS_GYM_ENABLED))) {
			handleGyms(context, session, loc);
		}
		handleSpawns(context, session, loc);
	}
	
	private void handleGyms(PokeContext context, BotSession session, GeoLocation loc) {
		Double range = session.getUser().getSettings() == null ? null : Util.atod(Util.otos(
				session.getUser().getSettings().get(ExtendedAttributes.USER_SETTINGS_GYM_RANGE)));
		if (range == null || range == 0D) {
			range = 3000D;
		}
		Integer minLevel = session.getUser().getSettings() == null ? null : Util.otoi(
				session.getUser().getSettings().get(ExtendedAttributes.USER_SETTINGS_GYM_LEVEL));
		if (minLevel == null || minLevel == 0) {
			minLevel = 4;
		}
		QueryOptions qo = new QueryOptions();
		BoundingCoordinates coords = loc.boundingCoordinates(range);
		qo.addFilter(Filter.gt(ExtendedAttributes.GYM_RAID_LEVEL, (minLevel - 1)));
		qo.addFilter(Filter.gt(ExtendedAttributes.GYM_RAID_END, new Date(new Date().getTime() + 1800000L)));
		qo.addFilter(Filter.gt(ExtendedAttributes.GYM_LATITUDE, coords.getX().getLatitudeInDegrees()));
		qo.addFilter(Filter.gt(ExtendedAttributes.GYM_LONGITUDE, coords.getX().getLongitudeInDegrees()));
		qo.addFilter(Filter.lt(ExtendedAttributes.GYM_LATITUDE, coords.getY().getLatitudeInDegrees()));
		qo.addFilter(Filter.lt(ExtendedAttributes.GYM_LONGITUDE, coords.getY().getLongitudeInDegrees()));
		Iterator<String> it = null;
		try {
			it = context.search(Gym.class, qo);
			if (it != null) {
				while (running && it.hasNext()) {
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
			for (Entry<String, Object> entry : attrs.entrySet()) {
				if (entry.getKey().endsWith("-enabled") && Util.otob(entry.getValue())) {
					Integer pokemonId = Util.atoi(entry.getKey().split("-")[0]);
					Double range = Util.atod(attrs.getString(String.format("%s-range", pokemonId)));
					handleSpawns(context, session, loc, pokemonId, range);
				}
			}
		}
	}
	
	private void handleSpawns(PokeContext context, BotSession session,
			GeoLocation loc, Integer pokemonId, Double distance) {
		try {
			Pokemon pokemon = context.getUniqueObject(Pokemon.class,
					Filter.eq(ExtendedAttributes.POKEMON_POKEMON_ID, pokemonId));
			BoundingCoordinates coords = loc.boundingCoordinates(distance);
			QueryOptions qo = new QueryOptions();
			qo.addFilter(Filter.eq(ExtendedAttributes.SPAWN_POKEMON, pokemon));
			qo.addFilter(Filter.gt(ExtendedAttributes.SPAWN_DISAPPEAR_TIME, new Date()));
			qo.addFilter(Filter.gt(ExtendedAttributes.SPAWN_LATITUDE, coords.getX().getLatitudeInDegrees()));
			qo.addFilter(Filter.gt(ExtendedAttributes.SPAWN_LONGITUDE, coords.getX().getLongitudeInDegrees()));
			qo.addFilter(Filter.lt(ExtendedAttributes.SPAWN_LATITUDE, coords.getY().getLatitudeInDegrees()));
			qo.addFilter(Filter.lt(ExtendedAttributes.SPAWN_LONGITUDE, coords.getY().getLongitudeInDegrees()));
			Iterator<String> it = context.search(Spawn.class, qo);
			if (it != null) {
				while (running && it.hasNext()) {
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
				if (!checkForDuplicates(context, session, spawn)) {
					try {
						UserNotification notif = announceSpawn(session, spawn);
						context.saveObject(notif);
						Auditor auditor = new Auditor(context);
						auditor.log(Auditor.SYSTEM, AuditAction.SEND_SPAWN_NOTIFICATION, session.getUser().getName());
						context.commitTransaction();
					} catch (TelegramApiException ex) {
						LOG.warn("Could not handle spawn: " + ex.getMessage(), ex);
					}
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error handling Spawn: " + ex.getMessage(), ex);
		}
	}
	
	private boolean checkForDuplicates(PokeContext context,
			BotSession session, Spawn spawn) throws GeneralException {
		boolean found = false;
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.like(ExtendedAttributes.POKE_OBJECT_NAME, String.format("%s:%s:",
				session.getUser().getName(), Spawn.class.getSimpleName()), Filter.MatchMode.START));
		Iterator<String> it = context.search(UserNotification.class, qo);
		if (it != null) {
			while (it.hasNext()) {
				UserNotification notif = context.getObjectById(UserNotification.class, it.next());
				Spawn s = context.getObjectByName(Spawn.class, notif.getName().split(":")[2]);
				if (spawn.getDisappearTime().getTime() == s.getDisappearTime().getTime()
						&& spawn.getPokemon().getId().equals(s.getPokemon().getId())) {
					found = true;
					break;
				}
			}
		}
		return found;
	}

	@SuppressWarnings("deprecation")
	private UserNotification announceSpawn(BotSession session, Spawn spawn) throws TelegramApiException {
		TelegramBot bot = Environment.getEnvironment().getTelegramBot();
		SendMessage msg = new SendMessage();
		msg.setChatId(session.getChatId());
		msg.setText(formatSpawn(session, spawn));
		Message m = bot.sendMessage(msg);
		String messageId = session.getChatId() + ":" + m.getMessageId();
		SendLocation loc = new SendLocation();
		loc.setChatId(session.getChatId());
		loc.setLatitude(Util.atof(Util.otos(spawn.getLatitude())));
		loc.setLongitude(Util.atof(Util.otos(spawn.getLongitude())));
		loc.setReplyMarkup(new BotKeyboard());
		m = bot.sendLocation(loc);
		UserNotification n = new UserNotification();
		n.setName(String.format("%s:%s:%s", session.getUser().getName(),
				spawn.getClass().getSimpleName(), spawn.getName()));
		n.setExpiration(spawn.getDisappearTime());
		n.setMessageId(messageId + "-" + session.getChatId() + ":" + m.getMessageId());
		return n;
	}
	
	private void handleGym(PokeContext context, BotSession session, Gym gym) {
		String name = String.format("%s:%s:%s", session.getUser().getName(),
				gym.getClass().getSimpleName(), gym.getName());
		try {
			if (context.getObjectByName(UserNotification.class, name) == null) {
				try {
					UserNotification notif = announceGym(context, session, gym);
					context.saveObject(notif);
					Auditor auditor = new Auditor(context);
					auditor.log(Auditor.SYSTEM, AuditAction.SEND_RAID_NOTIFICATION, session.getUser().getName());
					context.commitTransaction();
				} catch (TelegramApiException ex) {
					LOG.warn("Could not handle gym: " + ex.getMessage(), ex);
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error handling Gym: " + ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("deprecation")
	private UserNotification announceGym(PokeContext context, BotSession session,
			Gym gym) throws TelegramApiException, GeneralException {
		TelegramBot bot = Environment.getEnvironment().getTelegramBot();
		SendMessage msg = new SendMessage();
		msg.setChatId(session.getChatId());
		msg.setText(formatGym(session, gym));
		Message m = bot.sendMessage(msg);
		String messageId = session.getChatId() + ":" + m.getMessageId();
		SendLocation loc = new SendLocation();
		loc.setChatId(session.getChatId());
		loc.setLatitude(Util.atof(Util.otos(gym.getLatitude())));
		loc.setLongitude(Util.atof(Util.otos(gym.getLongitude())));
		loc.setReplyMarkup(new BotKeyboard());
		m = bot.sendLocation(loc);
		UserNotification n = new UserNotification();
		n.setName(String.format("%s:%s:%s", session.getUser().getName(),
				gym.getClass().getSimpleName(), gym.getName()));
		n.setExpiration(gym.getRaidEnd());
		n.setMessageId(messageId + "-" + session.getChatId() + ":" + m.getMessageId());
		return n;
	}
	
	private String formatGym(BotSession session, Gym gym) {
		return String.format("RAID: %s [ Level: %s, CP: %s, %s: %s, Team: %s ] %s: %sm - %s: %s",
				Localizer.localize(session.getUser(), String.format("pokemon_%s", gym.getRaidPokemon().getPokemonId())),
				Util.separateNumber(gym.getRaidLevel()), Util.separateNumber(gym.getRaidCp()),
				Localizer.localize(session.getUser(), "gym"), gym.getDisplayName(),
				gym.getTeam() == null ? "n/a" : Localizer.localize(session.getUser(), "team_" + gym.getTeam().getTeamId()),
				Localizer.localize(session.getUser(), "distance"),
				Util.separateNumber(Math.round(calculateDistance(session, gym.getLatitude(), gym.getLongitude()))),
				Localizer.localize(session.getUser(), "end"), Localizer.localize(session.getUser(), gym.getRaidEnd()));
	}
	
	private String formatSpawn(BotSession session, Spawn spawn) {
		return String.format("PKM: %s - %s: %sm - %s: %s", Localizer.localize(session.getUser(),
				String.format("pokemon_%s", spawn.getPokemon().getPokemonId())), Localizer.localize(session.getUser(), "distance"),
				Util.separateNumber(Math.round(calculateDistance(session, spawn.getLatitude(), spawn.getLongitude()))),
				Localizer.localize(session.getUser(), "end"), Localizer.localize(session.getUser(), spawn.getDisappearTime()));
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
}
