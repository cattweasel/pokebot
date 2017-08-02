package net.cattweasel.pokebot.scheduler;

import net.cattweasel.pokebot.tools.GeneralException;

public class TaskResultExistsException extends GeneralException {

	private static final long serialVersionUID = -5292772950303105572L;

	public TaskResultExistsException(String msg) {
		super(msg);
	}
}
