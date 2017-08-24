package net.cattweasel.pokebot.tools;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Localizer {

	private static final Logger LOG = Logger.getLogger(Localizer.class);
	
	public static String localize(Locale locale, String key) {
		Properties props = new Properties();
		try {
			props.load(Localizer.class.getResourceAsStream(String.format(
					"/net/cattweasel/pokebot/web/messages/pokebot_%s.properties",
					locale.getLanguage().toLowerCase())));
		} catch (IOException ex) {
			LOG.error("Error loading properties: " + ex.getMessage(), ex);
		}
		return props.getProperty(key);
	}
}
