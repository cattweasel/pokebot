package net.cattweasel.pokebot.scheduler;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.TaskDefinition;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;
import net.cattweasel.pokebot.server.Environment;
import net.cattweasel.pokebot.server.Service;
import net.cattweasel.pokebot.server.Servicer;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class TaskManager {

	private PokeContext context;
	private String launcher;
	private TaskExecutor executor;

	private static final Logger LOG = Logger.getLogger(TaskManager.class);

	public TaskManager() {
	}

	public TaskManager(PokeContext context) {
		this.context = context;
	}

	public void setLauncher(String launcher) {
		this.launcher = launcher;
	}

	public void startScheduler() throws GeneralException {
		Servicer.removeSuppressedService("Task");
		Service svc = getTaskService();
		if (svc != null) {
			svc.start();
		}
	}

	private Service getTaskService() {
		Environment env = Environment.getEnvironment();
		return env.getService("Task");
	}

	public void suspendScheduler() throws GeneralException {
		Service svc = getTaskService();
		if (svc != null) {
			svc.suspend();
		}
	}

	public boolean isSchedulerRunning() {
		boolean running = false;
		Service svc = getTaskService();
		if (svc != null) {
			running = svc.isStarted();
		}
		return running;
	}

	public void terminateOrphanTasks() throws GeneralException {
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.isnull("completed"));
		for (TaskResult result : context.getObjects(TaskResult.class, qo)) {
			LOG.warn("Terminating orphaned TaskResult: " + result.getName());
			result.setCompleted(new Date());
			result.setCompletionStatus(TaskResult.CompletionStatus.TERMINATED);
			context.saveObject(result);
			context.commitTransaction();
			unblockQuartzTriggers(result);
		}
	}

	private void unblockQuartzTriggers(TaskResult result) throws GeneralException {
		String name = result.getSchedule();
		if (name != null) {
			TaskSchedule sched = context.getObjectByName(TaskSchedule.class, name);
			if (sched != null) {
				context.saveObject(sched);
			}
		}
	}

	public void runNow(TaskSchedule sched) throws GeneralException {
		if (sched.getState() != null) {
			throw new GeneralException("Task Schedule already running: " + sched.getName());
		}
		saveLauncher(sched);
		sched.setState(TaskSchedule.State.EXECUTING);
		context.saveObject(sched);
	}

	private void saveLauncher(TaskSchedule sched) {
		if (launcher != null) {
			sched.setLauncher(launcher);
		} else if (sched.getLauncher() == null) {
			sched.setLauncher(context.getUsername());
		}
	}

	public void run(String name) throws GeneralException {
		run(getTaskDefinition(name));
	}

	public TaskDefinition getTaskDefinition(String name) throws GeneralException {
		TaskDefinition def = context.getObject(TaskDefinition.class, name);
		if (def == null) {
			throw new GeneralException("Task Definition not found: " + name);
		}
		return def;
	}

	public TaskSchedule run(TaskDefinition def) throws GeneralException {
		TaskSchedule sched = createImmediateSchedule(def);
		context.saveObject(sched);
		return sched;
	}

	private TaskSchedule createImmediateSchedule(TaskDefinition def) throws GeneralException {
		String name = def.getName();
		TaskResult existing = context.getObject(TaskResult.class, name);
		if (existing != null && existing.getCompleted() == null) {
			throw new GeneralException("Task already running: " + name);
		}
		TaskSchedule sched = new TaskSchedule();
		sched.setName(Util.uuid());
		sched.setDescription("Immediate Task Runner");
		//sched.setArguments(args);
		sched.setDefinition(def.getName());
		saveLauncher(sched);
		sched.setState(TaskSchedule.State.EXECUTING);
		return sched;
	}

	public TaskResult runWithResult(TaskDefinition def,
			Attributes<String, Object> args) throws GeneralException {
		def = context.getObjectById(TaskDefinition.class, def.getId());
		TaskSchedule sched = createImmediateSchedule(def);
		TaskResult result = createResult(sched, def, false);
		//sched.setArgument("resultId", result.getId());
		context.saveObject(sched);
		return result;
	}

	public TaskResult runSync(String defName) throws GeneralException {
		TaskSchedule sched = new TaskSchedule();
		sched.setDefinition(defName);
		saveLauncher(sched);
		return runSync(sched);
	}

	public TaskResult runSync(TaskDefinition def) throws GeneralException {
		return runSync(def.getName());
	}

	public TaskExecutor getTaskExecutor(TaskDefinition def) throws GeneralException {
		TaskExecutor exec = null;
		String className = def.getExecutor();
		if (className == null) {
			throw new GeneralException("Missing Task Executor for: " + def.getName());
		}
		try {
			exec = (TaskExecutor) Class.forName(className).newInstance();
		} catch (Exception ex) {
			LOG.error(ex);
		}
		if (exec == null) {
			throw new GeneralException("Unable to construct Executor for: " + def.getName());
		}
		return exec;
	}

	public TaskResult runSync(TaskSchedule schedule) throws GeneralException {
		TaskDefinition def = getTaskDefinition(schedule);
		TaskResult result = createResult(schedule, def, true);
		if (result != null) {
			try {
				executor = getTaskExecutor(def);
				context.commitTransaction();
				schedule.setDefinition(def.getName());
				Attributes<String, Object> attrs = def.getAttributes() == null
						? new Attributes<String, Object>() : def.getAttributes();
				executor.execute(context, schedule, result, attrs);
			} catch (Throwable t) {
				String message = t.getMessage();
				if (message == null) {
					message = t.toString();
				}
				LOG.error("Exception: [" + message + "]", t);
			} finally {
				try {
					context.reconnect();
					result.setCompleted(new Date());
					result.setCompletionStatus(TaskResult.CompletionStatus.SUCCESS);
					context.saveObject(result);
					context.commitTransaction();
				} catch (Throwable t) {
					LOG.error("Exception during final Save of Task Result", t);
				}
			}
		}
		executor = null;
		return result;
	}

	public TaskDefinition getTaskDefinition(TaskSchedule sched) throws GeneralException {
		String defid = sched.getDefinition();
		TaskDefinition def = context.getObject(TaskDefinition.class, defid);
		if (def == null) {
			throw new GeneralException("Task Definition not found:" + defid);
		}
		return def;
	}

	public boolean isTaskRunning(String taskName, String resultName) throws GeneralException {
		boolean isTaskRunning = false;
		TaskSchedule existing = context.getObject(TaskSchedule.class, taskName);
		if (existing == null) {
			TaskResult existingResult = context.getObject(TaskResult.class, resultName);
			if (existingResult != null && existingResult.getCompleted() == null) {
				isTaskRunning = true;
			}
		} else if (existing.getState() == TaskSchedule.State.EXECUTING) {
			isTaskRunning = true;
		}
		return isTaskRunning;
	}

	private TaskResult createResult(TaskSchedule sched, TaskDefinition def,
			boolean launching) throws GeneralException {
		TaskResult result = null;
		String name = sched.getName();
		if (name == null) {
			name = def.getName();
		}
		TaskResult temp = new TaskResult();
		temp.setName(name);
		temp.setSchedule(sched.getName());
		temp.setDefinition(def.getName());
		String launcher = sched.getLauncher();
		if (launcher == null || "".equals(launcher.trim())) {
			launcher = "System";
		}
		temp.setLauncher(launcher);
		if (launching) {
			temp.setLaunched(new Date());
		}
		boolean tryUnqualifiedName = true;
		TaskResult existing = context.getObjectByName(TaskResult.class, name);
		if (existing != null) {
			if (existing.getCompleted() == null) {
				temp = null;
				throw new GeneralException("Task already running: " + def.getName());
			} else {
				TaskDefinition.ResultAction action = def.getResultAction();
				if (action == null) {
					action = TaskDefinition.ResultAction.DELETE;
				}
				if (action == TaskDefinition.ResultAction.DELETE) {
					context.removeObject(existing);
					context.commitTransaction();
				} else if (action == TaskDefinition.ResultAction.RENAME) {
					saveQualifiedResult(existing, false);
				} else {
					if (action == TaskDefinition.ResultAction.CANCEL) {
						temp = null;
						String msg = "A Result for a previous Execution of Task '"
								+ def.getName() + "' still exists.";
						throw new TaskResultExistsException(msg);
					}
					if (action == TaskDefinition.ResultAction.RENAME_NEW) {
						tryUnqualifiedName = false;
					}
				}
			}
		}
		if (temp != null) {
			saveQualifiedResult(temp, tryUnqualifiedName);
			result = temp;
			sched.setResult(temp.getName());
			context.saveObject(sched);
		}
		return result;
	}

	public void saveQualifiedResult(TaskResult result, boolean tryUnqualified) throws GeneralException {
		boolean saved = false;
		boolean newObject = result.getId() == null;
		String origname = result.getName();
		if (tryUnqualified) {
			try {
				QueryOptions ops = new QueryOptions();
				ops.addFilter(Filter.eq("name", origname));
				int count = context.countObjects(TaskResult.class, ops);
				if (count == 0) {
					context.saveObject(result);
					context.commitTransaction();
					saved = true;
				}
			} catch (Throwable t) {
				if (newObject) {
					result.setId(null);
				}
			}
		}
		String name = unqualify(result.getName());
		if (!saved) {
			int qual = getNextQualifier(name);
			int maxAttempts = 20;
			Random r = new Random();
			for (int i = 0; i < maxAttempts && !saved; i++) {
				if ((i + 1) % 4 == 0) {
					long sleep = r.nextInt(4900) + 100;
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						LOG.info("Interrupting qualified Naming Sleep");
					}
				}
				result.setName(qualify(name, qual));
				try {
					context.saveObject(result);
					context.commitTransaction();
					saved = true;
				} catch (Throwable t) {
					try {
						context.reconnect();
						if (newObject) {
							result.setId(null);
						}
					} catch (Throwable t2) {
						i = maxAttempts;
					}
					qual++;
				}
			}
		}
		if (!saved) {
			try {
				result.setName(name + " " + System.currentTimeMillis());
				context.saveObject(result);
				context.commitTransaction();
			} catch (Throwable t) {
				throw new GeneralException("Cannot create Task Result");
			}
		}
	}

	private int getNextQualifier(String name) throws GeneralException {
		int qualifier = 1;
		name = unqualify(name);
		QueryOptions ops = new QueryOptions();
		ops.addFilter(Filter.like("name", name, Filter.MatchMode.START));
		for (TaskResult result : context.getObjects(TaskResult.class, ops)) {
			int q = getQualifier(result.getName());
			if (q >= qualifier) {
				qualifier = q + 1;
			}
		}
		return qualifier;
	}

	private String unqualify(String name) {
		int delim = name.lastIndexOf(" - ");
		if (delim > 0) {
			int dlen = " - ".length();
			String remainder = name.substring(delim + dlen);
			int qualifier = Util.atoi(remainder);
			if (qualifier > 0) {
				name = name.substring(0, delim);
			}
		}
		return name;
	}

	private String qualify(String name, int number) {
		return name + " - " + Util.itoa(number);
	}

	private int getQualifier(String name) {
		int qualifier = 0;
		int delim = name.lastIndexOf(" - ");
		if (delim > 0) {
			int dlen = " - ".length();
			String remainder = name.substring(delim + dlen);
			qualifier = Util.atoi(remainder);
		}
		return qualifier;
	}

	public boolean terminate() {
		boolean terminated = false;
		if (executor != null) {
			terminated = executor.terminate();
		}
		return terminated;
	}

	public TaskResult awaitTask(TaskSchedule sched, int seconds) throws Exception {
		TaskResult result = null;
		String resultName = sched.getDefinition();
		for (int iterations = 0; iterations < seconds; iterations++) {
			pause(1);
			context.reconnect();
			result = context.getObject(TaskResult.class, resultName);
			if (result != null && result.getCompleted() != null) {
				pause(1);
				break;
			}
			if (iterations >= seconds) {
				throw new GeneralException("Timeout waiting for Task Completion: " + resultName);
			}
		}
		return result;
	}

	public static void pause(int seconds) {
		try {
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException e) {
		}
	}

	public boolean terminate(TaskResult result) throws GeneralException {
		boolean terminated = false;
		TaskSchedule sched = null;
		result.setCompletionStatus(TaskResult.CompletionStatus.TERMINATED);
		context.saveObject(result);
		String schedId = result.getSchedule();
		if (schedId != null) {
			sched = context.getObject(TaskSchedule.class, schedId);
		}
		if (sched != null) {
			terminate(sched);
			terminated = true;
		}
		return terminated;
	}

	public void terminate(TaskSchedule sched) throws GeneralException {
		if (sched != null) {
			sched.setState(TaskSchedule.State.TERMINATED);
			context.saveObject(sched);
		}
	}
}
