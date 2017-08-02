package net.cattweasel.pokebot.tools.xml;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.cattweasel.pokebot.tools.GeneralException;

public class AbstractXmlObject {
	
	public String toXml() throws GeneralException {
		StringWriter writer = new StringWriter();
		try {
			JAXBContext context = JAXBContext.newInstance(getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, writer);
		} catch (JAXBException ex) {
			throw new GeneralException(ex);
		}
		return writer.toString();
	}
}
