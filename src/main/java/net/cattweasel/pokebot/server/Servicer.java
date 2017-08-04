package net.cattweasel.pokebot.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.ServiceDefinition;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class Servicer extends Thread {

	static List<String> _suppressedServices;
	int _cycleSeconds;
	int _definitionRefreshSeconds;
	boolean _terminate;
	boolean _ping;
	List<Service> _services;
	String _forcedExecution;

	private static final Logger LOG = Logger.getLogger(Servicer.class);

	public static void setSuppressedServices(String[] names) {
		_suppressedServices = new ArrayList<String>(Arrays.asList(names));
	}

	public static void addSuppressedServices(String name) {
		if (_suppressedServices == null) {
			_suppressedServices = new ArrayList<String>();
		}
		_suppressedServices.add(name);
	}

	public static void removeSuppressedService(String name) {
		if (_suppressedServices != null) {
			_suppressedServices.remove(name);
		}
	}

	public Servicer() {
		super("ServerThread");
		this._cycleSeconds = 1;
		this._definitionRefreshSeconds = 60;
		setDaemon(true);
		setPriority(10);

		/*
		 * ServiceDefinition def = new ServiceDefinition();
		 * def.setName("Cache"); CacheService svc = new CacheService();
		 * svc.setStarted(true); add(svc);
		 */

	}

	public void start(PokeContext con) throws GeneralException {
		configure(con);
		start();
	}

	public void configure(PokeContext con) throws GeneralException {
		LOG.info("Servicer initializing");
		List<ServiceDefinition> definitions = getDefinitions(con);
		for (ServiceDefinition def : definitions) {
			install(con, def);
		}
		if (this._services != null) {
			for (Service service : this._services) {
				if (isAutoStart(service)) {
					LOG.info("Auto starting service: " + service.getName());
					service.start();
				}
			}
		}
	}

	private List<ServiceDefinition> getDefinitions(PokeContext con) throws GeneralException {
		List<ServiceDefinition> definitions = con.getObjects(ServiceDefinition.class);
		if (definitions == null) {
			definitions = new ArrayList<ServiceDefinition>();
		}
		boolean taskFound = false;
		for (ServiceDefinition def : definitions) {
			if ("Task".equals(def.getName())) {
				taskFound = true;
			}
		}
		if (!taskFound) {
			definitions.add(bootstrapService("Task"));
		}
		return definitions;
	}

	private ServiceDefinition bootstrapService(String name) {
		ServiceDefinition def = new ServiceDefinition();
		def.setName(name);
		def.setExecutor("net.cattweasel.pokebot.server." + name + "Service");
		return def;
	}

	private <T extends Service> void install(PokeContext con, ServiceDefinition def) throws GeneralException {
		String executor = def.getExecutor();
		try {
			if (executor == null) {
				executor = "net.cattweasel.pokebot.server." + def.getName() + "Service";
			}
			LOG.debug("Attempting to load Service class: " + executor);
			Service service = (Service) Class.forName(executor).newInstance();
			service.setDefinition(def);
			add(service);
			if (def.getInterval() != 0) {
				service.setInterval(def.getInterval());
			}
			service.configure(con);
		} catch (Throwable t) {
			LOG.error("Unable to install service: " + def.getName());
			LOG.error(t);
		}
	}

	public void add(Service service) {
		if (service != null) {
			LOG.info("Adding service: " + service.getName());
			if (this._services == null) {
				this._services = new ArrayList<Service>();
			}
			this._services.add(service);
		}
	}

	private boolean isAutoStart(Service service) {
		ServiceDefinition def = service.getDefinition();
		boolean runit = false;
		if (def == null) {
			runit = true;
		} else {
			runit = true;
			if (runit && _suppressedServices != null) {
				for (int i = 0; i < _suppressedServices.size(); i++) {
					if (((String) _suppressedServices.get(i)).equals(def.getName())) {
						runit = false;
						break;
					}
				}
			}
		}
		return runit;
	}

	public List<Service> getServices() {
		return this._services;
	}

	public Service getService(String name) {
		Service found = null;
		if (name != null && this._services != null) {
			for (Service service : this._services) {
				if (name.equals(service.getName())) {
					found = service;
					break;
				}
			}
		}
		return found;
	}

	public void run() {
		LOG.info("Starting service thread");
		int definitionAge = 0;
		while (!this._terminate) {
			try {
				LOG.debug("Starting service thread cycle");
				boolean refreshDefinitions = false;
				if (definitionAge >= this._definitionRefreshSeconds) {
					refreshDefinitions = true;
					definitionAge = 0;
				}
				processServices(refreshDefinitions);
				try {
					Thread.sleep(this._cycleSeconds * 1000);
				} catch (InterruptedException ie) {
					LOG.info("Continuing after main cycle interruption");
				}
				definitionAge += this._cycleSeconds;
				this._ping = true;
			} catch (Throwable t) {
				LOG.error("Exception during cycle: " + t.toString());
			}
		}
		LOG.info("Stopping service thread");
	}

	public void terminate() {
		this._terminate = true;
		if (this._services != null) {
			for (Service service : this._services) {
				try {
					service.terminate();
				} catch (Throwable t) {
					LOG.error("Unable to terminate service: " + service.getName());
					LOG.error(t);
				}
			}
		}
		interrupt();
	}

	public boolean ping() {
		this._ping = false;
		interrupt();
		for (int i = 0; i < 10 && !this._ping; i++) {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException ie) {
			}
		}
		return this._ping;
	}

	/*public void reconfigure(PokeContext context, Configuration config) {
		try {
			if (log.isInfoEnabled()) {
				log.info("Servicer reconfiguring after system configuration change");
			}
			if (this._services != null) {
				for (Service service : this._services) {
					service.configure(context);
				}
			}
		} catch (Throwable t) {
			log.error("Unable to reconfigure Services after Configuration change", t);
		}
	}*/

	public void reconfigure(PokeContext context, ServiceDefinition def) {
		try {
			if (this._services != null) {
				for (Service service : this._services)
					if (service.getName().equals(def.getName())) {
						if (LOG.isInfoEnabled()) {
							LOG.info("Servicer reconfiguring " + def.getName()
									+ " service after ServiceDefinition change");
						}
						reconfigure(context, def, service);
					}
			}
		} catch (Throwable t) {
			LOG.error("Unable to reconfigure Service " + def.getName() + " after ServiceDefinition change", t);
		}
	}

	private void processServices(boolean refreshDefinitions) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext("Services");
			if (refreshDefinitions) {
				refreshDefinitions(context);
			}
			if (this._forcedExecution != null) {
				Service service = getService(this._forcedExecution);
				if (service != null) {
					LOG.info("Forcing execution of service: " + service.getName());
					execute(context, service);
				} else {
					LOG.error("Unable to force execution of unknown service: " + this._forcedExecution);
				}
				this._forcedExecution = null;
			} else if (this._services != null) {
				List<Service> ready = new ArrayList<Service>();
				for (Service service : this._services) {
					boolean runit = false;
					if (service.isStarted()) {
						Date last = service.getLastExecute();
						if (last == null) {
							runit = true;
						} else {
							int interval = service.getInterval();
							Date now = new Date();
							Date then = new Date(last.getTime() + interval * 1000);
							runit = now.compareTo(then) >= 0;
							if (!runit && LOG.isDebugEnabled()) {
								LOG.debug("Service " + service.getName() + " waiting until " + Util.dateToString(then));
							}
						}
					}
					if (runit) {
						ready.add(service);
					}
				}
				for (Service svc : ready) {
					if (svc.isPriority()) {
						execute(context, svc);
					}
				}
				for (Service svc : ready) {
					if (!svc.isPriority()) {
						execute(context, svc);
					}
				}
			}
		} catch (Throwable t) {
			LOG.error("Unable to initialize service context", t);
		} finally {
			try {
				if (context != null) {
					PokeFactory.releaseContext(context);
				}
			} catch (Throwable t) {
				LOG.error(t);
			}
		}
	}

	private void execute(PokeContext context, Service service) throws Exception {
		try {
			service.setLastExecute(new Date());
			service.execute(context);
		} catch (Throwable t) {
			LOG.error("Unable to execute service: " + service.getName());
			LOG.error(t);
			LOG.error(Util.stackToString(t));
		} finally {
			service.setLastEnd(new Date());
		}
	}

	private void refreshDefinitions(PokeContext context) {
		try {
			for (Service service : this._services) {
				boolean needsRefresh = false;
				ServiceDefinition def = service.getDefinition();
				if (def != null) {
					Date newmod = getModificationDate(context, def);
					if (newmod != null) {
						Date oldmod = def.getModified();
						if (oldmod == null) {
							oldmod = def.getCreated();
						}
						needsRefresh = oldmod == null || !oldmod.equals(newmod);
					}
					if (needsRefresh) {
						if (LOG.isInfoEnabled()) {
							LOG.info("Refreshing ServiceDefinition for " + def.getName());
						}
						def = (ServiceDefinition) context.getObjectByName(ServiceDefinition.class, def.getName());
						reconfigure(context, def, service);
					}
				}
			}
		} catch (Throwable t) {
			LOG.error("Exception during refreshDefinition", t);
		}
	}

	private void reconfigure(PokeContext context, ServiceDefinition def, Service service) throws GeneralException {
		service.setDefinition(def);
		service.configure(context);
		if (isAutoStart(service)) {
			if (!service.isStarted()) {
				service.start();
			}
		} else if (service.isStarted()) {
			service.suspend();
		}
	}

	public static <T extends PokeObject> Date getModificationDate(PokeContext context, T src) {
		Date result = null;
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.eq("name", src.getName()));
		try {
			for (PokeObject gbo : context.getObjects(src.getClass(), qo)) {
				result = gbo.getModified();
				if (result == null) {
					result = gbo.getCreated();
				}
			}
			if (result == null && LOG.isInfoEnabled()) {
				LOG.info("No modification date for: " + src.getClass().getSimpleName() + ":" + src.getName());
			}
		} catch (Throwable t) {
			LOG.error("Exception trying to get modification date for: " + src.getClass().getSimpleName() + ":"
					+ src.getName());
			LOG.error(t);
			LOG.error(Util.stackToString(t));
		}
		return result;
	}
}
