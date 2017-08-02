package net.cattweasel.pokebot.object;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

import net.cattweasel.pokebot.tools.Util;

/**
 * An extension of HashMap that adds convenience coercion methods.
 * 
 * @author Benjamin Wesp
 * 
 */
@XmlRootElement(name = "Attributes")
public class Attributes <K, V> extends HashMap<K, V> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Returns a property as string (if available).
	 * 
	 * @param key The attribute to be resolved
	 * @return The resolved attribute if found - null otherwise
	 */
	public String getString(String key) {
		return Util.otos(get(key));
	}
	
	/**
	 * Returns a property as integer (if available).
	 * 
	 * @param key The attribute to be resolved
	 * @return The resolved attribute if found - null otherwise 
	 */
	public Integer getInt(String key) {
		return Util.atoi(getString(key));
	}
	
	/**
	 * Returns a property as boolean (if available).
	 * 
	 * @param key The attribute to be resolved
	 * @return The resolved attribute if found - null otherwise
	 */
	public Boolean getBoolean(String key) {
		return Util.otob(get(key));
	}
	
	/**
	 * Returns a property as date (if available).
	 * 
	 * @param key The attribute to be resolved
	 * @return The resolved attribute if found - null otherwise
	 * @throws ParseException In case of an invalid date
	 */
	public Date getDate(String key) throws ParseException {
		return (Date) get(key);
	}
}
