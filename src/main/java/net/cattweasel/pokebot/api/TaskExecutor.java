package net.cattweasel.pokebot.api;

import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;

/**
 * Interface of an object that is able to execute tasks.
 * 
 * @author Benjamin Wesp
 *
 */
public abstract interface TaskExecutor {

	/**
	 * Executes a specific task.
	 * 
	 * @param context The DSContext to be used during execution
	 * @param schedule The @{net.cattweasel.pokebot.object.TaskSchedule} for this task
	 * @param result The @{net.cattweasel.pokebot.object.TaskResult} for this task
	 * @param attributes Possible parameters for this task
	 * @throws Exception In case of any critical error
	 */
	void execute(PokeContext context, TaskSchedule schedule, TaskResult result,
			Attributes<String, Object> attributes) throws Exception;

	/**
	 * Terminates a running task.
	 * 
	 * @return True if termination was successful, false otherwise
	 */
	boolean terminate();
}
