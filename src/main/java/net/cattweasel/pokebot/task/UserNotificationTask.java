package net.cattweasel.pokebot.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.api.TelegramBot;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;
import net.cattweasel.pokebot.object.UserNotification;
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
	private static final String ARG_RANGE = "range";
	
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
		Double range = Util.atod(Util.otos(session.get(ARG_RANGE)));
		range = range == null || range == 0D ? 3000D : range;
		BoundingCoordinates coords = loc.boundingCoordinates(range);
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.or(Filter.eq(ARG_RAID_LEVEL, 4), Filter.eq(ARG_RAID_LEVEL, 5)));
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
			
			// TODO: SPAWNS !!!
			
		} catch (GeneralException ex) {
			LOG.error("Error handling Session: " + ex.getMessage(), ex);
		}
	}
	
	private void handleGym(PokeContext context, BotSession session, Gym gym) {
		String name = String.format("%s:%s:%s", session.getUser().getName(),
				gym.getClass().getSimpleName(), gym.getName());
		try {
			if (context.getObjectByName(UserNotification.class, name) == null) {
				try {
					announceGym(session, gym);
				} catch (TelegramApiException ex) {
					LOG.error("Error announcing Gym: " + ex.getMessage(), ex);
				}
				UserNotification n = new UserNotification();
				n.setName(name);
				n.setExpiration(gym.getRaidEnd());
				context.saveObject(n);
				context.commitTransaction();
			}
		} catch (GeneralException ex) {
			LOG.error("Error handling Gym: " + ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("deprecation")
	private void announceGym(BotSession session, Gym gym) throws TelegramApiException {
		TelegramBot bot = Environment.getEnvironment().getTelegramBot();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String txt = String.format("RAID: %s [ Level: %s, CP: %s, Arena: %s ] %sStart: %s Uhr - Ende: %s Uhr",
				gym.getRaidPokemon().getName(), gym.getRaidLevel(), gym.getRaidCp(),
				gym.getDisplayName(), calculateDistance(session, gym.getLatitude(), gym.getLongitude()),
				sdf.format(gym.getRaidStart()), sdf.format(gym.getRaidEnd()));
		SendMessage msg = new SendMessage();
		msg.setChatId(session.getChatId());
		msg.setText(txt);
		bot.sendMessage(msg);
		SendLocation loc = new SendLocation();
		loc.setChatId(session.getChatId());
		loc.setLatitude(Util.atof(Util.otos(gym.getLatitude())));
		loc.setLongitude(Util.atof(Util.otos(gym.getLongitude())));
		bot.sendLocation(loc);
	}
	
	private String calculateDistance(BotSession session, Double latitude, Double longitude) {
		StringBuilder sb = new StringBuilder();
		if (session != null && session.get(ARG_LATITUDE) != null && session.get(ARG_LONGITUDE) != null) {
			GeoLocation loc = GeoLocation.fromDegrees(latitude, longitude);
			Double lat = Util.atod(Util.otos(session.get(ARG_LATITUDE)));
			Double lon = Util.atod(Util.otos(session.get(ARG_LONGITUDE)));
			Double distance = loc.distanceTo(GeoLocation.fromDegrees(lat, lon));
			sb.append(String.format("Entfernung: %sm - ", Math.round(distance)));
		}
		return sb.toString();
	}
}
