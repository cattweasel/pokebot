package net.cattweasel.pokebot.tools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.User;

public class Localizer {

	private static final Logger LOG = Logger.getLogger(Localizer.class);
	
	public static String localize(User user, String key) {
		return localize(resolveLocale(user), key);
	}
	
	public static String localize(Locale locale, String key) {
		Properties props = new Properties();
		try {
			props.load(new InputStreamReader(Localizer.class.getResourceAsStream(String.format(
					"/net/cattweasel/pokebot/web/messages/pokebot_%s.properties",
					locale.getLanguage().toLowerCase())), StandardCharsets.UTF_8));
		} catch (IOException ex) {
			LOG.error("Error loading properties: " + ex.getMessage(), ex);
		}
		return props.getProperty(key);
	}
	
	public static String localize(User user, Date date) {
		return localize(user, date, false);
	}
	
	public static String localize(User user, Date date, boolean includeDate) {
		return localize(resolveLocale(user), date, includeDate);
	}
	
	public static String localize(Locale locale, Date date, boolean includeDate) {
		String result = null;
		SimpleDateFormat sdf = resolveDateFormat(locale, includeDate);
		if (Locale.ENGLISH == locale) {
			result = sdf.format(date);
		} else {
			result = String.format("%s Uhr", sdf.format(date));
		}
		return result;
	}
	
	private static Locale resolveLocale(User user) {
		Locale locale = Locale.GERMAN;
		if (user != null && user.getSettings() != null
				&& user.getSettings().get(ExtendedAttributes.USER_SETTINGS_LANGUAGE) != null) {
			if ("en".equalsIgnoreCase(user.getSettings().getString(ExtendedAttributes.USER_SETTINGS_LANGUAGE))) {
				locale = Locale.ENGLISH;
			}
		}
		return locale;
	}
	
	private static SimpleDateFormat resolveDateFormat(Locale locale, boolean includeDate) {
		SimpleDateFormat sdf = null;
		if (Locale.ENGLISH == locale) {
			if (includeDate) {
				sdf = new SimpleDateFormat("yyyy-MM-dd - hh:mm a");
			} else {
				sdf = new SimpleDateFormat("hh:mm a");
			}
		} else {
			if (includeDate) {
				sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
			} else {
				sdf = new SimpleDateFormat("HH:mm");
			}
		}
		return sdf;
	}
}
