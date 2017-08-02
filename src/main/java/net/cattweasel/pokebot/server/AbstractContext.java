package net.cattweasel.pokebot.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.tools.GeneralException;

public abstract class AbstractContext implements PokeContext {

	Map<String, Object> _properties;

	public AbstractContext() {
	}

	public PokeContext getContext() {
		return this;
	}

	public void setUsername(String name) {
	}

	public String getUsername() {
		return null;
	}

	public Object clone() throws java.lang.CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void startTransaction() throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public void commitTransaction() throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public void rollbackTransaction() throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public void close() throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> T getObjectById(Class<T> cls, String id) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> T getObjectByName(Class<T> cls, String name) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public <T extends PokeObject> T getObject(Class<T> cls, String idOrName) throws GeneralException {
		PokeObject obj = null;
		try {
			obj = getObjectByName(cls, idOrName);
			if (obj == null) {
				obj = getObjectById(cls, idOrName);
			}
		} catch (Exception e) {
			throw new GeneralException(e);
		}
		return (T) obj;
	}

	public <T extends PokeObject> List<T> getObjects(Class<T> cls) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> List<T> getObjects(Class<T> cls, QueryOptions options) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> Iterator<String> search(Class<T> cls) throws GeneralException {
		throw new UnsupportedOperationException();
	}
	
	public <T extends PokeObject> Iterator<String> search(Class<T> cls, QueryOptions options) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> Iterator<String> search(Class<T> cls, QueryOptions options, String property)
			throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> Iterator<Object[]> search(Class<T> cls, QueryOptions options, List<String> properties)
			throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public Iterator<?> search(String query, Map<String, Object> args, QueryOptions options) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> int countObjects(Class<T> cls, QueryOptions ops) throws GeneralException {
		throw new UnsupportedOperationException("not supported");
	}

	public <T extends PokeObject> void removeObjects(Class<T> cls, QueryOptions options) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public void reconnect() {
	}

	public void setProperty(String name, Object value) {
		if (name != null) {
			if (value != null) {
				if (this._properties == null)
					this._properties = new HashMap<String, Object>();
				this._properties.put(name, value);
			} else if (this._properties != null) {
				this._properties.remove(name);
			}
		}
	}

	public Object getProperty(String name) {
		return this._properties != null ? this._properties.get(name) : null;
	}
}
