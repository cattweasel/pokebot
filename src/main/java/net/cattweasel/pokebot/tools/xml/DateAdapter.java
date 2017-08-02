package net.cattweasel.pokebot.tools.xml;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XmlAdapter for converting between dates and their original value.
 * 
 * @author Benjamin Wesp
 *
 */
public class DateAdapter extends XmlAdapter<Long, Date> {
	
	/**
	 * Converts a timestamp into a date.
	 * 
	 * @param value The timestamp to be converted
	 * @return The date object
	 * @throws Exception In case of any error
	 */
	@Override
	public Date unmarshal(Long value) throws Exception {
		Date date = value == null ? null : new Date(value);
		return date;
	}

	/**
	 * Converts a date into a timestamp.
	 * 
	 * @param v The date to be converted
	 * @return The timestamp object
	 * @throws Exception In case of any error
	 */
	@Override
	public Long marshal(Date value) throws Exception {
		Long timestamp = value == null ? null : value.getTime();
		return timestamp;
	}
}
