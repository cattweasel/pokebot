package net.cattweasel.pokebot.server;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.object.ImportAction;
import net.cattweasel.pokebot.object.ImportContainer;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;
import net.cattweasel.pokebot.tools.xml.AbstractXmlObject;
import net.cattweasel.pokebot.tools.xml.XMLParser;

public class Importer {

	private PokeContext context;
	
	private static final Logger LOG = Logger.getLogger(Importer.class);
	
	public Importer(PokeContext context) {
		this.context = context;
	}
	
	public void importXml(String xml) throws GeneralException {
		try {
			Class<? extends AbstractXmlObject> clazz = XMLParser.getXmlRootElement(xml);
			Object object = XMLParser.parseXml(clazz, xml);
			if (object instanceof ImportContainer) {
				ImportContainer c = (ImportContainer) object;
				for (ImportAction a : c.getActions()) {
					importXml(Util.readFile(a.getLocation()));
				}
			} else {
				PokeObject obj = (PokeObject) object;
				LOG.debug("Importing Object: " + obj);
				context.saveObject(obj);
				context.commitTransaction();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new GeneralException(ex);
		}
	}
}
