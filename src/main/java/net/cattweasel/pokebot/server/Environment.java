package net.cattweasel.pokebot.server;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.generics.BotSession;

import net.cattweasel.pokebot.api.PersistenceManager;
import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.api.RuleRunner;
import net.cattweasel.pokebot.api.TelegramBot;
import net.cattweasel.pokebot.scheduler.QuartzSchedulerStarter;
import net.cattweasel.pokebot.tools.GeneralException;

public class Environment {

	private static Environment _singleton;
	private DataSource _dataSource;
	private SessionFactory _sessionFactory;
	private PersistenceManager _persistenceManager;
	private RuleRunner _ruleRunner;
	private QuartzSchedulerStarter _taskScheduler;
	private Servicer _servicer;
	private TelegramBot _bot;
	private BotSession _botSession;
	
	private static final Logger LOG = Logger.getLogger(Environment.class);
	
	public static Environment getEnvironment() {
		return _singleton;
	}

	public Environment() {
		_singleton = this;
		this._servicer = new Servicer();
	}

	public void setDataSource(DataSource ds) {
		this._dataSource = ds;
	}

	public DataSource getSpringDataSource() {
		return this._dataSource;
	}
	
	public void setTelegramBot(TelegramBot bot) {
		this._bot = bot;
	}
	
	public TelegramBot getTelegramBot() {
		return this._bot;
	}

	public int getActiveConnections() {
		int active = 0;
		if (this._dataSource instanceof BasicDataSource) {
			active = ((BasicDataSource) this._dataSource).getNumActive();
		}
		return active;
	}

	public void setSessionFactory(SessionFactory sf) {
		this._sessionFactory = sf;
	}

	public SessionFactory getSessionFactory() {
		return this._sessionFactory;
	}

	public void setPersistenceManager(PersistenceManager pm) {
		this._persistenceManager = pm;
	}

	public PersistenceManager getPersistenceManager() {
		return this._persistenceManager;
	}
	
	public RuleRunner getRuleRunner() {
		return this._ruleRunner;
	}

	public void setRuleRunner(RuleRunner rr) {
		this._ruleRunner = rr;
	}

	public QuartzSchedulerStarter getTaskScheduler() {
		return _taskScheduler;
	}

	public void setTaskScheduler(QuartzSchedulerStarter scheduler) {
		this._taskScheduler = scheduler;
	}

	public Servicer getServicer() {
		return _servicer;
	}

	public void setServicer(Servicer servicer) {
		_servicer = servicer;
	}

	public void start() throws GeneralException {
		PokeContext con = PokeFactory.createContext("System");
		try {
			this._servicer.start(con);
			TelegramBotsApi api = new TelegramBotsApi();
			this._botSession = api.registerBot(_bot);
		} catch (TelegramApiException ex) {
			throw new GeneralException(ex);
		} finally {
			if (con != null) {
				PokeFactory.releaseContext(con);
			}
		}
	}

	public void stop() {
		this._botSession.stop();
		this._servicer.terminate();
		long start = System.currentTimeMillis();
		try {
			this._servicer.join(2000L);
			LOG.debug("Waited " + (System.currentTimeMillis() - start)
					+ " ms for thread to stop: " + this._servicer
					+ "; state = " + this._servicer.getState());
		} catch (InterruptedException e) {
			LOG.info("Interrupted while waiting for servicer.");
		}
	}

	public Service getService(String name) {
		return this._servicer.getService(name);
	}

	public List<Service> getServices() {
		return this._servicer.getServices();
	}

	public Service getTaskService() {
		return getService("Task");
	}
}
