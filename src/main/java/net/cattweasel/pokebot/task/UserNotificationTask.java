package net.cattweasel.pokebot.task;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;

/**
 * This task is used to send out notifications to users.
 * 
 * @author Benjamin Wesp
 *
 */
public class UserNotificationTask implements TaskExecutor {
	
	//private boolean running = true;
	
	@Override
	public void execute(PokeContext context, TaskSchedule schedule, TaskResult result,
			Attributes<String, Object> attributes) throws Exception {
		
		// TODO: Jede BotSession bedienen!!!
		
	}

	@Override
	public boolean terminate() {
		//running = false; 
		return true;
	}
}
