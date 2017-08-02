package net.cattweasel.pokebot.scheduler;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.TaskSchedule;
import net.cattweasel.pokebot.tools.GeneralException;

@PersistJobDataAfterExecution
public class PokeJobFactory implements Job, JobFactory {

	private static final Logger LOG = Logger.getLogger(PokeJobFactory.class);

	public void setScheduler(Scheduler sched) {
		try {
			sched.setJobFactory(this);
		} catch (SchedulerException e) {
			LOG.error(e);
		}
	}

	public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
		Job job = null;
		JobDetail detail = bundle.getJobDetail();
		JobDataMap args = detail.getJobDataMap();
		String defname = null;
		if (args != null) {
			defname = args.getString("executor");
		}
		if (defname == null) {
			defname = detail.getKey().getName();
		}
		if (defname != null) {
			PokeContext context = null;
			try {
				context = PokeFactory.createContext("Scheduler");
				TaskSchedule sched = (TaskSchedule) context.getObject(TaskSchedule.class, detail.getKey().getName());
				if (sched == null) {
					sched = new TaskSchedule();
				}
				job = new JobAdapter(sched);
			} catch (Throwable t) {
				job = new JobAdapter(null);
			} finally {
				try {
					PokeFactory.releaseContext(context);
				} catch (GeneralException e) {
					LOG.warn("Failed releasing PokeContext: " + e.getLocalizedMessage(), e);
				}
			}
		}
		return job;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.error("Attempted to execute PokeJobFactory!");
	}
}
