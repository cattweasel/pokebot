package net.cattweasel.pokebot.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.ServiceDefinition;
import net.cattweasel.pokebot.server.Importer;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class StartupContextListener implements ServletContextListener {

	private static final String SPRING_CONFIG_FILE = "pokebot.xml";
	
	private ClassPathXmlApplicationContext context;

	private static final Logger LOG = Logger.getLogger(StartupContextListener.class);

	public void contextInitialized(ServletContextEvent event) {
		this.context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILE);
		try {
			initObjects();
		} catch (GeneralException ex) {
			LOG.error(ex);
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		if (this.context != null) {
			this.context.close();
			this.context = null;
		}
	}
	
	private void initObjects() throws GeneralException {
		int count = 0;
		PokeContext context = null;
		try {
			context = PokeFactory.createContext("Initialization");
			count = context.countObjects(ServiceDefinition.class);
			if (count == 0) {
				LOG.info("Importing init.xml to initialize the system.");
				String xml = Util.readFile("init.xml");
				Importer importer = new Importer(context);
				importer.importXml(xml);
				LOG.info("Initialization complete.");
			}
		} finally {
			if (null != context) {
				PokeFactory.releaseContext(context);
			}
		}
	}
}
