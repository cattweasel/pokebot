package net.cattweasel.pokebot.server;

import java.util.Date;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.object.ServiceDefinition;
import net.cattweasel.pokebot.tools.GeneralException;

public abstract class Service {

	protected String _name;
	protected ServiceDefinition _definition;
	protected int _interval;
	protected Date _lastExecute;
	protected Date _lastEnd;
	protected boolean _started;

	public Service() {
	}

	public ServiceDefinition getDefinition() {
		return this._definition;
	}

	public void setDefinition(ServiceDefinition def) {
		this._definition = def;
	}

	public String getName() {
		return this._definition != null ? this._definition.getName() : this._name;
	}

	public int getInterval() {
		return this._interval;
	}

	public void setInterval(int i) {
		this._interval = i;
	}

	public Date getLastExecute() {
		return _lastExecute == null ? null : new Date(_lastExecute.getTime());
	}

	public void setLastExecute(Date d) {
		this._lastExecute = d == null ? null : new Date(d.getTime());
	}

	public Date getLastEnd() {
		return _lastEnd == null ? null : new Date(_lastEnd.getTime());
	}

	public void setLastEnd(Date d) {
		this._lastEnd = d == null ? null : new Date(d.getTime());
	}

	public void setStarted(boolean b) {
		this._started = b;
	}

	public boolean isStarted() {
		return this._started;
	}

	public String getStatusString() {
		return this._started ? "Started" : "Stopped";
	}

	public boolean isPriority() {
		return false;
	}

	public void configure(PokeContext context) throws GeneralException {
	}

	public void execute(PokeContext context) throws GeneralException {
	}

	public void start() throws GeneralException {
		this._started = true;
	}

	public void suspend() throws GeneralException {
		this._started = false;
	}

	public void terminate() throws GeneralException {
		this._started = false;
	}

	public void wake() {
	}

	public boolean ping() {
		return isStarted();
	}
}
