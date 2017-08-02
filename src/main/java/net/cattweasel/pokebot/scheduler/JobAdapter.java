package net.cattweasel.pokebot.scheduler;

import org.apache.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.TaskSchedule;

@PersistJobDataAfterExecution
public class JobAdapter implements InterruptableJob {

	TaskSchedule _schedule;
	TaskManager _taskManager;

	private static final Logger LOG = Logger.getLogger(JobAdapter.class);

	public JobAdapter(TaskSchedule sched) {
		this._schedule = sched;
	}

	public void interrupt() throws UnableToInterruptJobException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		if (this._schedule != null) {
			PokeContext ctx = null;
			try {
				ctx = PokeFactory.createContext("Scheduler");
				this._taskManager = new TaskManager(ctx);
				this._taskManager.runSync(this._schedule);
				if (this._schedule.getCronExpression() == null) {
					ctx.removeObject(this._schedule);
					ctx.commitTransaction();
				}
			} catch (Throwable t) {
				LOG.error("The " + this._schedule.getName()
						+ " task failed to execute", t);
			} finally {
				if (ctx != null) {
					try {
						PokeFactory.releaseContext(ctx);
					} catch (Throwable t) {
						LOG.error(t);
					}
				}
			}
		}
	}
}
