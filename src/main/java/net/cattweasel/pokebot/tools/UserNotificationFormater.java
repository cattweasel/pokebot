package net.cattweasel.pokebot.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.Spawn;

public class UserNotificationFormater {

	private final Attributes<String, Object> settings;
	private final Attributes<String, Object> session;
	
	public UserNotificationFormater(Attributes<String, Object> settings,
			Attributes<String, Object> session) {
		this.settings = settings;
		this.session = session;
	}
	
	public String formatGym(Gym gym) {
		return String.format("RAID: %s [ Level: %s, CP: %s, %s: %s ] %s: %sm - %s: %s",
				Localizer.localize(getLocale(), String.format("pokemon_%s", gym.getRaidPokemon().getPokemonId())),
				Util.separateNumber(gym.getRaidLevel()), Util.separateNumber(gym.getRaidCp()),
				Localizer.localize(getLocale(), "gym"), gym.getDisplayName(), Localizer.localize(getLocale(), "distance"),
				Util.separateNumber(Math.round(calculateDistance(gym.getLatitude(), gym.getLongitude()))),
				Localizer.localize(getLocale(), "end"), formatTimestamp(gym.getRaidEnd()));
	}
	
	public String formatSpawn(Spawn spawn) {
		return String.format("PKM: %s - %s: %sm - %s: %s", Localizer.localize(getLocale(),
				String.format("pokemon_%s", spawn.getPokemon().getPokemonId())), Localizer.localize(getLocale(), "distance"),
				Util.separateNumber(Math.round(calculateDistance(spawn.getLatitude(), spawn.getLongitude()))),
				Localizer.localize(getLocale(), "end"), formatTimestamp(spawn.getDisappearTime()));
	}
	
	private Double calculateDistance(Double lat, Double lng) {
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
	
	private String formatTimestamp(Date timestamp) {
		String result = null;
		SimpleDateFormat sdf = getSimpleDateFormat(getLocale());
		if (Locale.ENGLISH == getLocale()) {
			result = sdf.format(timestamp);
		} else {
			result = String.format("%s Uhr", sdf.format(timestamp));
		}
		return result;
	}
	
	private Locale getLocale() {
		Locale locale = null;
		String lang = settings == null || settings.get(ExtendedAttributes.USER_SETTINGS_LANGUAGE) == null
				? null : settings.getString(ExtendedAttributes.USER_SETTINGS_LANGUAGE);
		if ("en".equalsIgnoreCase(lang)) {
			locale = Locale.ENGLISH;
		} else {
			locale = Locale.GERMAN;
		}
		return locale;
	}
	
	private SimpleDateFormat getSimpleDateFormat(Locale locale) {
		SimpleDateFormat sdf = null;
		if (Locale.ENGLISH == locale) {
			sdf = new SimpleDateFormat("hh:mm a");
		} else {
			sdf = new SimpleDateFormat("HH:mm");
		}
		return sdf;
	}
}
