package net.cattweasel.pokebot.task;

import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.Spawn;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;
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
	
	private void removeOrphanSpawns(PokeContext context) {
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.lt("disappearTime", new Date()));
		Iterator<String> it = null;
		try {
			it = context.search(Spawn.class, qo);
			if (it != null) {
				while (running && it.hasNext()) {
					Spawn spawn = context.getObjectById(Spawn.class, it.next());
					if (spawn != null) {
						LOG.debug("Removing spawn: " + spawn);
						context.removeObject(spawn);
					}
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error removing orphan spawns: " + ex.getMessage(), ex);
		}
	}
	
	private void removeOrphanRaids(PokeContext context) {
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.and(Filter.notnull("raidEnd"), Filter.lt("raidEnd", new Date())));
		Iterator<String> it = null;
		try {
			it = context.search(Gym.class, qo);
			if (it != null) {
				while (running && it.hasNext()) {
					Gym gym = context.getObjectById(Gym.class, it.next());
					if (gym != null) {
						LOG.debug("Removing raid: " + gym);
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
