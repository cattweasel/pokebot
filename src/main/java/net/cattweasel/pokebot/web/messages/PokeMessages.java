package net.cattweasel.pokebot.web.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

public class PokeMessages extends ResourceBundle {

	private static class UTF8Control extends Control {
        
		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
				boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, BUNDLE_EXTENSION);
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, CHARSET));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }
	
	private static final String BUNDLE_NAME = "net.cattweasel.pokebot.web.messages.pokebot";
	private static final String BUNDLE_EXTENSION = "properties";
	private static final String CHARSET = StandardCharsets.UTF_8.name();
	private static final Control UTF8_CONTROL = new UTF8Control();
	
	public PokeMessages() {
		setParent(ResourceBundle.getBundle(BUNDLE_NAME, FacesContext
				.getCurrentInstance().getViewRoot().getLocale(), UTF8_CONTROL));
	}
	
	@Override
	protected Object handleGetObject(String key) {
		return parent.getObject(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return parent.getKeys();
	}
}
