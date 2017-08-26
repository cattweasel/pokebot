package net.cattweasel.pokebot.scheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.TaskSchedule;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class QuartzPersistenceManager extends AbstractPersistenceManager {

	//static final String ERROR_ATTRIBUTE = TaskSchedule.class.getName() + ".lastLaunchError";

	private Scheduler _scheduler;

	private static final Logger log = Logger.getLogger(QuartzPersistenceManager.class);

	public QuartzPersistenceManager() {
	}

	public void setScheduler(Scheduler s) {
		_scheduler = s;
	}

	public Scheduler getScheduler() {
		return _scheduler;
	}

	public Scheduler getGlobalScheduler() {
		return _scheduler;
	}

	private void checkClass(Class<?> cls) throws GeneralException {
		if (_scheduler == null) {
			throw new GeneralException("QuartzPersistenceManager: No Scheduler");
		}
		if (cls != TaskSchedule.class) {
			throw new GeneralException("QuartzPersistenceManager: unsupported class " + cls.getName());
		}
	}

	public <T extends PokeObject> Iterator<Object[]> search(Class<T> cls,
			QueryOptions options, List<String> properties)
			throws GeneralException {
		checkClass(cls);
		String filterName = null;
		List<Filter> filters = null;
		if (options != null) {
			filters = options.getFilters();
		}
		if (filters != null && filters.size() != 1) {
			throw new GeneralException("Unsupported filter");
		}
		if (filters != null) {
			Filter f = (Filter) filters.get(0);
			if (f.getMode() != Filter.Mode.EQ) {
				throw new GeneralException("Unsupported filter");
			}
			if (!"name".equals(f.getProperty())) {
				throw new GeneralException("Unsupported filter");
			}
			Object value = f.getValue();
			if (!(value instanceof String)) {
				throw new GeneralException("Unsupported filter");
			}
			filterName = (String) value;
		}
		if (properties == null || properties.size() != 1) {
			throw new GeneralException("Unsupported projection");
		}
		if (!((String) properties.get(0)).equals("id")) {
			throw new GeneralException("Unsupported projection");
		}
		List<Object[]> result = new ArrayList<Object[]>();
		if (filterName != null) {
			TaskSchedule ts = (TaskSchedule) getObjectByName(
					TaskSchedule.class, filterName);
			if (ts != null) {
				Object[] row = new Object[1];
				row[0] = filterName;
				result.add(row);
			}
		} else {
			try {
				List<String> jobs = getJobNames();
				if (jobs.size() > 0) {
					Iterator<?> jit = jobs.iterator();
					while (jit.hasNext()) {
						Object[] row = new Object[1];
						row[0] = jit.next();
						result.add(row);
					}
				}
			} catch (SchedulerException e) {
				throw new GeneralException(e);
			}
		}
		return result.iterator();
	}

	private List<String> getJobNames() throws SchedulerException {
		List<String> jobnames = new ArrayList<String>();
		for (String group : _scheduler.getJobGroupNames()) {
			for (JobKey jobKey : _scheduler.getJobKeys(GroupMatcher
					.jobGroupEquals(group)))
				jobnames.add(jobKey.getName());
		}
		return jobnames;
	}

	public <T extends PokeObject> T getObjectById(Class<T> cls, String id)
			throws GeneralException {
		return getObjectByName(cls, id);
	}

	@SuppressWarnings("unchecked")
	public <T extends PokeObject> T getObjectByName(Class<T> cls,
			String name) throws GeneralException {
		TaskSchedule sched = null;
		checkClass(cls);
		try {
			JobDetail jd = _scheduler.getJobDetail(JobKey.jobKey(name));
			if (jd != null) {
				sched = new TaskSchedule();
				sched.setName(name);
				sched.setDescription(jd.getDescription());
				sched.setId(name);
				List<JobExecutionContext> executing = _scheduler.getCurrentlyExecutingJobs();
				if (executing != null) {
					for (int i = 0; i < executing.size(); i++) {
						JobExecutionContext jc = (JobExecutionContext) executing
								.get(i);
						if (name.equals(jc.getJobDetail().getKey().getName())) {
							sched.setState(TaskSchedule.State.EXECUTING);
						}
					}
				}
				List<? extends Trigger> jdt = _scheduler.getTriggersOfJob(JobKey.jobKey(name));
				if (jdt.size() > 0) {
					Trigger trig = null;
					Date lastExecution = null;
					Date nextExecution = null;
					Iterator<?> trgit = jdt.iterator();
					while (trgit.hasNext()) {
						trig = (Trigger) trgit.next();
						if (log.isDebugEnabled()) {
							log.debug("\n******Trigger Debug*****");
							Date now = new Date();
							Date nextFireTime = trig.getFireTimeAfter(now);
							int j = 0;
							while (nextFireTime != null && j < 10) {
								log.debug(trig.getKey().getName() + ": "
										+ nextFireTime);
								nextFireTime = trig
										.getFireTimeAfter(nextFireTime);
								j++;
							}
							log.debug("******Trigger Debug*****\n");
						}
						Date last;
						Date next;
						if (trig instanceof CronTrigger) {
							CronTrigger cronTrig = (CronTrigger) trig;
							last = cronTrig.getPreviousFireTime();
							next = cronTrig.getNextFireTime();
							sched.setCronExpression(cronTrig
									.getCronExpression());
						} else {
							last = trig.getPreviousFireTime();
							next = trig.getNextFireTime();
							if (last == null) {
								last = trig.getStartTime();
							}
						}
						if (last != null && (lastExecution == null || last.compareTo(lastExecution) > 0)) {
							lastExecution = last;
						}
						if (next != null && (nextExecution == null || next.compareTo(nextExecution) < 0)) {
							nextExecution = next;
						}
					}
					sched.setLastExecution(lastExecution);
					if (trig != null) {
						if (trig.mayFireAgain()) {
							sched.setNextExecution(nextExecution);
						}
						Trigger.TriggerState state = _scheduler
								.getTriggerState(TriggerKey.triggerKey(trig
										.getKey().getName()));
						if (Trigger.TriggerState.NONE != state || state == Trigger.TriggerState.PAUSED) {
							sched.setState(TaskSchedule.State.SUSPENDED);
						} else if (state == Trigger.TriggerState.ERROR) {
							sched.setState(TaskSchedule.State.ERROR);
						}
					}
				}
				JobDataMap jdm = jd.getJobDataMap();
				if (jdm != null) {
					Iterator<?> it = jdm.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<Object, Object> ent = (Map.Entry<Object, Object>) it.next();
						Object key = ent.getKey();
						Object value = ent.getValue();
						if ("taskDefinition".equals(key)) {
							sched.setDefinition((String) value); 
						} else if ("taskResult".equals(key)) {
							sched.setResult((String) value);
						} else if ("launcher".equals(key)) {
							sched.setLauncher((String) value);
						} else {
							log.warn("unhandled attribute: " + key);
						}
					}
				}
			}
		} catch (SchedulerException e) {
			throw new GeneralException(e);
		}
		return (T) sched;
	}

	public void saveObject(PokeObject obj) throws GeneralException {
		checkClass(obj.getClass());

		//boolean update = false;
		
		if (!(obj instanceof TaskSchedule)) {
			throw new UnsupportedOperationException("Cannot handle " + obj.getClass().getName());
		}
		
		TaskSchedule js = (TaskSchedule) obj;
		String id = js.getId();
		String name = js.getName();
		if (id == null) {
			if (name != null) {
				TaskSchedule existing = (TaskSchedule) getObjectById(
						TaskSchedule.class, name);
				if (existing != null) {
					throw new GeneralException("qtz_persitence_mgr_err_dup_task_name");
				}
			} else {
				throw new GeneralException(
						"Can't save TaskSchedule without a name");
			}
		} else if (name == null) {
			name = id;
		} else if (!name.equals(id)) {
			TaskSchedule existing = (TaskSchedule) getObjectById(
					TaskSchedule.class, id);
			if (existing != null) {
				removeObject(existing);
				//update = true;
			}
		}
		try {
			//Attributes args = js.getArguments();
			JobDataMap jdMap = new JobDataMap();
			
			jdMap.put("taskDefinition", js.getDefinition());
			jdMap.put("taskResult", js.getResult());
			jdMap.put("launcher", js.getLauncher());
			
			/*if (args != null) {
				Map stringifiedArgs = new HashMap();
				if (args.getProperties() != null) {
					for (Property prop : args.getProperties()) {
						String key = prop.getName();
						Object val = prop.getValue();
						if ((key != null) && (val != null)) {
							stringifiedArgs.put(key, val.toString());
						}
					}
				}
				if (null != js.getLastLaunchError()) {
					stringifiedArgs.put(ERROR_ATTRIBUTE,
							js.getLastLaunchError());
				}
				jdMap = new JobDataMap(stringifiedArgs);
			}*/
			
			JobDetail jd = JobBuilder.newJob(JobAdapter.class)
					.withIdentity(name).withDescription(js.getDescription())
					.setJobData(jdMap).storeDurably().build();
			_scheduler.addJob(jd, true);
			
			String cron = js.getCronExpression();
			List<? extends Trigger> jdt = _scheduler.getTriggersOfJob(JobKey.jobKey(name));
			if (jdt.size() > 0) {
				Iterator<?> trgit = jdt.iterator();
				while (trgit.hasNext()) {
					Trigger tr = (Trigger) trgit.next();
					if (tr instanceof CronTrigger) {
						String cronString = ((CronTrigger) tr).getCronExpression();
						if (!cron.equals(cronString))
							_scheduler.unscheduleJob(TriggerKey.triggerKey(tr
									.getKey().getName()));
					} else {
						_scheduler.unscheduleJob(TriggerKey.triggerKey(tr
								.getKey().getName()));
					}
				}
			}
			
			
			// hier muss die magie geschehen... folgende m??glichkeiten:
			// 1.) runNow == true => schedule einmalig schedulen (sofort) und anschlie??end l??schen
			// 2.) runOnce == true => schedule einmalig schedulen, nach ausf??hrung entfernen
			// 3.) cronExpression != null => schedule nach cron expression erstellen
			
			boolean runNow = Util.otob(js.isRunNow());
			boolean runOnce = Util.otob(js.isRunOnce());
			Date nextExecution = js.getNextExecution();
			
			//System.out.println("\n** debug: runNow => " + runNow);
			//System.out.println("** debug: runOnce => " + runOnce);
			//System.out.println("** debug: nextExecution => " + nextExecution);
			//System.out.println("** debug: cron => " + cron);
			
			if (runNow) {
				
				System.out.println("** debug: this schedule should be run once now");
				throw new UnsupportedOperationException("not yet implemented"); // TODO !!!
				
			} else if (runOnce && nextExecution != null) {
				
				System.out.println("** debug: this schedule should be run once on " + nextExecution);
				throw new UnsupportedOperationException("not yet implemented"); // TODO !!!
				
			} else {
				String triggerName = name;
				CronTrigger t = (CronTrigger) TriggerBuilder
						.newTrigger()
						.withIdentity(triggerName)
						.forJob(name)
						.withSchedule(
								CronScheduleBuilder.cronSchedule(cron))
						.startAt(new Date()).build();
				Trigger existing = _scheduler.getTrigger(TriggerKey
						.triggerKey(triggerName));
				if (existing == null) {
					_scheduler.scheduleJob(t);
				} else if (existing instanceof CronTrigger) {
					CronTrigger et = (CronTrigger) existing;
					if (!cron.equals(et.getCronExpression())) {
						_scheduler.rescheduleJob(
								TriggerKey.triggerKey(name), t);
					}
				} else {
					_scheduler.rescheduleJob(TriggerKey.triggerKey(name), t);
				}
			}
		} catch (SchedulerException e) {
			throw new GeneralException(e);
		}
	}

	public void importObject(PokeObject obj) throws GeneralException {
		PokeObject existing = getObjectByName(TaskSchedule.class,
				obj.getName());
		if (null != existing) {
			removeObject(existing);
		}
		saveObject(obj);
	}

	public void removeObject(PokeObject obj) throws GeneralException {
		checkClass(obj.getClass());
		try {
			List<? extends Trigger> jdt = _scheduler
					.getTriggersOfJob(JobKey.jobKey(obj.getName()));
			if (jdt.size() > 0) {
				Iterator<?> trigit = jdt.iterator();
				while (trigit.hasNext()) {
					Trigger t = (Trigger) trigit.next();
					_scheduler.unscheduleJob(TriggerKey.triggerKey(t.getKey()
							.getName(), t.getKey().getGroup()));
				}
			}
			_scheduler.deleteJob(JobKey.jobKey(obj.getName()));
		} catch (SchedulerException e) {
			throw new GeneralException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends PokeObject> List<T> getObjects(Class<T> cls)
			throws GeneralException {
		List<T> list = new ArrayList<T>();
		checkClass(cls);
		try {
			List<String> jobs = getJobNames();
			if (jobs.size() > 0) {
				Iterator<?> jit = jobs.iterator();
				while (jit.hasNext()) {
					String jobName = (String) jit.next();
					PokeObject js = getObjectByName(TaskSchedule.class,
							jobName);
					if (js != null) {
						list.add((T) js);
					}
				}
			}
		} catch (SchedulerException e) {
			throw new GeneralException(e);
		}
		return list;
	}

	/*public <T extends PokeObject> Iterator<Object[]> search(Class<T> cls,
			QueryOptions options) throws GeneralException {
		List<T> objs = getObjects(cls, options);
		return null != objs ? objs.iterator() : new ArrayList<T>().iterator();
	}*/

	@SuppressWarnings("unchecked")
	public <T extends PokeObject> List<T> getObjects(Class<T> cls,
			QueryOptions options) throws GeneralException {
		List<TaskSchedule> schedules = (List<TaskSchedule>) getObjects(cls);
		if (options != null) {
			schedules = filterSchedules(schedules);
			
			/*List orderings = options.getOrderings();
			if ((schedules.size() > 0) && (!orderings.isEmpty())) {
				if (1 != orderings.size()) {
					throw new GeneralException(
							"Can only sort task schedules by a single property.");
				}
				QueryOptions.Ordering ordering = (QueryOptions.Ordering) orderings
						.get(0);
				Collections.sort(schedules,
						new TaskScheduleComparator(ordering.getColumn(),
								ordering.isAscending()));
			}*/
			
		}
		return (List<T>) schedules;
	}

	private List<TaskSchedule> filterSchedules(
			List<TaskSchedule> schedules) throws GeneralException {
		return schedules; // TODO
	}

	public <T extends PokeObject> int countObjects(Class<T> cls) throws GeneralException {
		return countObjects(cls, new QueryOptions());
	}
	
	public <T extends PokeObject> int countObjects(Class<T> cls, QueryOptions options)
			throws GeneralException {
		int count = 0;
		checkClass(cls);
		List<T> objects = getObjects(cls, options);
		if (null != objects) {
			count = objects.size();
		}
		return count;
	}

	static class TaskScheduleComparator implements Comparator<TaskSchedule>, Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public static final String ATTR_NAME = "name";
		public static final String ATTR_DESCRIPTION = "description";
		public static final String ATTR_TYPE = "type";
		public static final String ATTR_DEFNAME = "defName";
		public static final String ATTR_NEXT_EXECUTION = "nextExecution";
		public static final String ATTR_LAST_EXECUTION = "lastExecution";

		boolean _isAcending;
		String _attr;

		public TaskScheduleComparator() {
			this._isAcending = true;
			this._attr = "name";
		}

		public TaskScheduleComparator(String attrName, boolean acending) {
			this._isAcending = acending;
			this._attr = attrName;
		}

		public int compare(TaskSchedule t1, TaskSchedule t2) {
			int compareValue = -1;
			if ("name".compareTo(this._attr) == 0 || "description".compareTo(this._attr) == 0
					|| "type".compareTo(this._attr) == 0 || "defName".compareTo(this._attr) == 0) {
				String s1 = t1.getName();
				String s2 = t2.getName();
				if ("description".compareTo(this._attr) == 0) {
					s1 = t1.getDescription();
					s2 = t2.getDescription();
				}
				if (this._isAcending) {
					if (s1 != null) {
						compareValue = s1.compareTo(s2);
					}
				} else if (s2 != null) {
					compareValue = s2.compareTo(s1);
				}
			} else if ("nextExecution".compareTo(this._attr) == 0
					|| "lastExecution".compareTo(this._attr) == 0) {
				Date d1 = t1.getNextExecution();
				Date d2 = t2.getNextExecution();
				if ("lastExecution".compareTo(this._attr) == 0) {
					d1 = t1.getLastExecution();
					d2 = t2.getLastExecution();
				}
				if (this._isAcending) {
					if (d1 != null) {
						if (d2 == null)
							compareValue = 1;
						else
							compareValue = d1.compareTo(d2);
					}
				} else if (d2 != null)
					if (d1 == null)
						compareValue = 1;
					else
						compareValue = d2.compareTo(d1);
			}
			return compareValue;
		}
	}
}
