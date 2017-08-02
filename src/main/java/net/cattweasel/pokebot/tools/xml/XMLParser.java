package net.cattweasel.pokebot.tools.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.tools.GeneralException;

/**
 * This class can parse and convert XML data into an @{link net.cattweasel.pokebot.tools.xml.AbstractXmlObject}.
 *  
 * @author Benjamin Wesp
 *
 */
public class XMLParser {

	private static final Logger LOG = Logger.getLogger(XMLParser.class);
	
	/**
	 * Parses XML data and converts it into an java object.
	 * 
	 * @param clazz The class of the resulting object
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.tools.xml.AbstractXmlObject}
	 * @param xml The XML data to be parsed
	 * @return The parsed java object
	 * @throws GeneralException In case of any error
	 */
	public static <T extends AbstractXmlObject> Object parseXml(Class<T> clazz, String xml) throws GeneralException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			InputStream istream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			Object result = unmarshaller.unmarshal(istream);
			return result;
		} catch (JAXBException ex) {
			throw new GeneralException(ex);
		} catch (UnsupportedEncodingException ex) {
			throw new GeneralException(ex);
		}
	}

	/**
	 * Public helper method for determining the object class.
	 * 
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.tools.xml.AbstractXmlObject}
	 * @param xml The XML data to be analyzed
	 * @return The class of the object if found, null otherwise
	 * @throws GeneralException In case of any error
	 */
	@SuppressWarnings("unchecked")
	public static <T extends AbstractXmlObject> Class<T> getXmlRootElement(String xml) throws GeneralException {
		Pattern p = Pattern.compile("<([a-zA-Z]+)(>| (.*))");
		Matcher m = p.matcher(xml);
		Class<T> result = null;
		if (m.find()) {
			try {
				result = (Class<T>) Class.forName("net.cattweasel.pokebot.object." + m.group(1));
			} catch (ClassNotFoundException ex) {
				LOG.error(ex);
			}
			if (result == null) {
				try {
					result = (Class<T>) Class.forName("net.cattweasel.pokebot.tools.xml." + m.group(1));
				} catch (ClassNotFoundException ex) {
					LOG.error(ex);
				}
			}
		}
		if (result == null) {
			throw new GeneralException("Class could not be determined");
		}
		return result;
	}
}
