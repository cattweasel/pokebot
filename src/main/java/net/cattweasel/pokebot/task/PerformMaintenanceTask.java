package net.cattweasel.pokebot.task;

import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.api.Terminator;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.OnetimeLink;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.Spawn;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.tools.GeneralException;

/**
 * This task is used to perform several system related background processes.
 * 
 * @author Benjamin Wesp
 *
 */
public class PerformMaintenanceTask implements TaskExecutor {
	
	private boolean running = true;
	
	private static final Logger LOG = Logger.getLogger(PerformMaintenanceTask.class);
	
	@Override
	public void execute(PokeContext context, TaskSchedule schedule, TaskResult result,
			Attributes<String, Object> attributes) throws Exception {
		if (running) {
			removeOrphanOnetimeLinks(context);
			context.commitTransaction();
		}
		if (running) {
			removeOrphanUserNotifications(context);
			context.commitTransaction();
		}
		if (running) {
			removeOrphanSpawns(context);
			context.commitTransaction();
		}
		if (running) {
			removeOrphanRaids(context);
			context.commitTransaction();
		}
	}

	@Override
	public boolean terminate() {
		running = false;
		return true;
	}
	
	private void removeOrphanOnetimeLinks(PokeContext context) {
		Terminator terminator = new Terminator(context);
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.lt(ExtendedAttributes.POKE_OBJECT_CREATED, new Date(new Date().getTime() - 180000L)));
		Iterator<String> it = null;
		try {
			it = context.search(OnetimeLink.class, qo);
			if (it != null) {
				while (running && it.hasNext()) {
					OnetimeLink link = context.getObjectById(OnetimeLink.class, it.next());
					if (link != null) {
						LOG.debug("Removing orphan onetime link: " + link);
						terminator.deleteObject(link);
					}
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error removing orphan onetime links: " + ex.getMessage(), ex);
		}
	}
	
	private void removeOrphanUserNotifications(PokeContext context) {
		Terminator terminator = new Terminator(context);
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.lt(ExtendedAttributes.USER_NOTIFICATION_EXPIRATION, new Date()));
		Iterator<String> it = null;
		try {
			it = context.search(UserNotification.class, qo);
			if (it != null) {
				while (running && it.hasNext()) {
					UserNotification notif = context.getObjectById(UserNotification.class, it.next());
					if (notif != null) {
						LOG.debug("Removing orphan notification: " + notif);
						terminator.deleteObject(notif);
					}
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error removing orphan notifications: " + ex.getMessage(), ex);
		}
	}
	
	private void removeOrphanSpawns(PokeContext context) {
		Terminator terminator = new Terminator(context);
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.lt(ExtendedAttributes.SPAWN_DISAPPEAR_TIME, new Date()));
		Iterator<String> it = null;
		try {
			it = context.search(Spawn.class, qo);
			if (it != null) {
				while (running && it.hasNext()) {
					Spawn spawn = context.getObjectById(Spawn.class, it.next());
					if (spawn != null) {
						LOG.debug("Removing orphan spawn: " + spawn);
						terminator.deleteObject(spawn);
					}
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error removing orphan spawns: " + ex.getMessage(), ex);
		}
	}
	
	private void removeOrphanRaids(PokeContext context) {
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.and(Filter.notnull(ExtendedAttributes.GYM_RAID_END),
				Filter.lt(ExtendedAttributes.GYM_RAID_END, new Date())));
		Iterator<String> it = null;
		try {
			it = context.search(Gym.class, qo);
			if (it != null) {
				while (running && it.hasNext()) {
					Gym gym = context.getObjectById(Gym.class, it.next());
					if (gym != null) {
						LOG.debug("Removing orphan raid: " + gym);
						gym.setRaidCp(null);
						gym.setRaidEnd(null);
						gym.setRaidLevel(null);
						gym.setRaidPokemon(null);
						gym.setRaidStart(null);
						context.saveObject(gym);
					}
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error removing orphan raids: " + ex.getMessage(), ex);
		}
	}
}
