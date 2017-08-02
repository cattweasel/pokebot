package net.cattweasel.pokebot.server;

import net.cattweasel.pokebot.tools.GeneralException;

public class SystemStarter {

	private Environment _env;

	public SystemStarter() {
	}

	public void setEnvironment(Environment env) {
		this._env = env;
	}

	public void springInit() throws GeneralException {
		if (this._env == null) {
			throw new GeneralException("SystemStarter has no Environment");
		}
		this._env.start();
	}

	public void springDestroy() {
		this._env.stop();
	}
}
