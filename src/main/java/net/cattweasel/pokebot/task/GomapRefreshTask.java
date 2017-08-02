package net.cattweasel.pokebot.task;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;

/**
 * This task is used to get the latest updates from gopmap.
 * 
 * @author Benjamin Wesp
 *
 */
public class GomapRefreshTask implements TaskExecutor {
	
	@Override
	public void execute(PokeContext context, TaskSchedule schedule, TaskResult result,
			Attributes<String, Object> attributes) throws Exception {
		
		// TODO
		
	}

	@Override
	public boolean terminate() {
		
		return true; // TODO
		
	}
}
