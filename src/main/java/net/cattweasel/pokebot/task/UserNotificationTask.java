package net.cattweasel.pokebot.task;

import java.util.Iterator;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;

/**
 * This task is used to send out notifications to users.
 * 
 * @author Benjamin Wesp
 *
 */
public class UserNotificationTask implements TaskExecutor {
	
	private boolean running = true;
	
	@Override
	public void execute(PokeContext context, TaskSchedule schedule, TaskResult result,
			Attributes<String, Object> attributes) throws Exception {
		Iterator<String> it = context.search(BotSession.class);
		if (it != null) {
			while (running && it.hasNext()) {
				BotSession session = context.getObjectById(BotSession.class, it.next());
				handleSession(context, session);
			}
		}
	}

	@Override
	public boolean terminate() {
		running = false; 
		return true;
	}
	
	private void handleSession(PokeContext context, BotSession session) {
		
		// TODO !!!
		
	}
}
