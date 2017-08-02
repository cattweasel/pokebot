package net.cattweasel.pokebot.server;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.scheduler.QuartzSchedulerStarter;
import net.cattweasel.pokebot.scheduler.TaskManager;
import net.cattweasel.pokebot.tools.GeneralException;

public class TaskService extends Service {

	QuartzSchedulerStarter _quartz;
	boolean _initialized;

	private static final Logger log = Logger.getLogger(TaskService.class);

	public TaskService() {
		this._name = "Task";
	}

	@Override
	public void configure(PokeContext context) {
		if (this._quartz == null) {
			Environment env = Environment.getEnvironment();
			this._quartz = env.getTaskScheduler();
		}
	}

	@Override
	public void start() throws GeneralException {
		this._started = false;
		if (this._quartz != null) {
			if (!this._initialized) {
				this._initialized = true;
				terminateOrphanTasks();
			}
			this._quartz.startScheduler();
			this._started = true;
		}
	}

	@Override
	public void suspend() throws GeneralException {
		this._started = false;
		if (this._quartz != null) {
			this._quartz.suspend();
		}
	}

	@Override
	public void terminate() throws GeneralException {
		this._started = false;
		if (this._quartz != null) {
			this._quartz.stopScheduler();
		}
	}

	private void terminateOrphanTasks() {
		log.info("Terminating orphan tasks");
		PokeContext save = null;
		try {
			save = PokeFactory.pushContext();
			PokeContext ours = PokeFactory.getCurrentContext();
			TaskManager tm = new TaskManager(ours);
			tm.terminateOrphanTasks();
		} catch (Throwable t) {
			log.error(t);
		} finally {
			try {
				PokeFactory.popContext(save);
			} catch (Throwable t2) {
				log.error(t2);
			}
		}
	}
}
