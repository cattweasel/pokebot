package net.cattweasel.pokebot.scheduler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import net.cattweasel.pokebot.tools.GeneralException;

public class QuartzSchedulerStarter {

	private Scheduler scheduler;

	private static final long MAX_WAIT_TIME = 2000L;

	private static final Logger log = Logger.getLogger(QuartzSchedulerStarter.class);

	public QuartzSchedulerStarter(Scheduler scheduler) throws SchedulerException {
		this.scheduler = scheduler;
	}

	public void startScheduler() throws GeneralException {
		try {
			log.info("Starting Quartz Scheduler...");
			this.scheduler.start();
		} catch (SchedulerException se) {
			throw new GeneralException(se);
		}
	}

	public void stopScheduler() throws GeneralException {
		try {
			shutdownAndWaitForThread();
		} catch (SchedulerException se) {
			throw new GeneralException(se);
		}
	}

	public boolean isSchedulerRunning() {
		boolean running = false;
		if (this.scheduler != null) {
			try {
				running = !this.scheduler.isInStandbyMode();
			} catch (SchedulerException se) {
				log.error(se);
			}
		}
		return running;
	}

	public void interruptScheduler() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	public void suspend() throws GeneralException {
		try {
			log.info("Suspending Quartz Scheduler...");
			this.scheduler.standby();
		} catch (SchedulerException se) {
			throw new GeneralException(se);
		}
	}

	public void resume() throws GeneralException {
		try {
			log.info("Resuming Quartz Scheduler...");
			this.scheduler.start();
		} catch (SchedulerException se) {
			throw new GeneralException(se);
		}
	}

	public void shutdownAndWaitForThread() throws SchedulerException {
		log.info("Shutting down Quartz Scheduler and waiting for Thread...");
		this.scheduler.shutdown(true);
		if (log.isDebugEnabled()) {
			listAllThreads();
		}
		log.debug("Removing Scheduler Threads..");
		for (Thread thread : getAllSchedulerThreads()) {
			waitOnThread(thread);
		}
		log.debug("Done removing Scheduler Threads");
	}

	private void waitOnThread(Thread thread) {
		if (thread == null) {
			log.debug("Unable to wait on null Thread");
		} else {
			long start = System.currentTimeMillis();
			try {
				thread.join(MAX_WAIT_TIME);
				log.debug("Waited " + (System.currentTimeMillis() - start)
						+ " ms for Thread to stop: " + thread + "; state = "
						+ thread.getState());
			} catch (InterruptedException e) {
				log.info("Interrupted while waiting for Quartz Thread: "
						+ thread.getName() + " to die.");
			}
		}
	}

	private void listAllThreads() {
		for (Thread thread : getAllSchedulerThreads()) {
			log.debug("Thread: " + thread.getName());
		}
	}

	private Thread[] getAllSchedulerThreads() {
		List<Thread> schedulerThreads = new ArrayList<Thread>();
		Thread[] threads = getAllThreads();
		for (Thread thread : threads) {
			if (thread.getName().toLowerCase().indexOf("quartzscheduler") > -1) {
				schedulerThreads.add(thread);
			}
		}
		return (Thread[]) schedulerThreads.toArray(new Thread[schedulerThreads.size()]);
	}

	private Thread[] getAllThreads() {
		ThreadGroup root = getRootThreadGroup();
		ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
		int nAlloc = thbean.getThreadCount();
		int n = 0;
		Thread[] threads;
		do {
			nAlloc *= 2;
			threads = new Thread[nAlloc];
			n = root.enumerate(threads, true);
		} while (n == nAlloc);
		return copyOf(threads, n);
	}

	private Thread[] copyOf(Thread[] threads, int length) {
		Thread[] copy = new Thread[length];
		System.arraycopy(threads, 0, copy, 0, Math.min(threads.length, length));
		return copy;
	}

	private ThreadGroup getRootThreadGroup() {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		ThreadGroup parentThreadGroup;
		while ((parentThreadGroup = threadGroup.getParent()) != null) {
			threadGroup = parentThreadGroup;
		}
		return threadGroup;
	}
}
