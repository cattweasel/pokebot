package net.cattweasel.pokebot.scheduler;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;

import net.cattweasel.pokebot.api.PersistenceManager;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.tools.GeneralException;

public abstract class AbstractPersistenceManager implements PersistenceManager, Cloneable {

	public AbstractPersistenceManager() {
	}

	public void startTransaction() {
	}

	public void commitTransaction() {
	}

	public void rollbackTransaction() {
	}

	public void close() {
	}
	
	public Connection getJdbcConnection() throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public <T extends PokeObject> T getObjectById(
			Class<T> cls, String id) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> T getObjectByName(
			Class<T> cls, String name) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> T getObject(
			Class<T> cls, String idOrName) throws GeneralException {
		T obj = getObjectByName(cls, idOrName);
		if (obj == null) {
			obj = getObjectById(cls, idOrName);
		}
		return obj;
	}

	public void saveObject(PokeObject obj) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public void removeObject(PokeObject obj) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> java.util.List<T> getObjects(
			Class<T> cls) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> java.util.List<T> getObjects(Class<T> cls, QueryOptions options)
			throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> java.util.List<T> getObjects(T example) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> int countObjects(Class<T> cls,
			QueryOptions ops) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> Iterator<String> search(Class<T> cls) throws GeneralException {
		throw new UnsupportedOperationException();
	}
	
	public <T extends PokeObject> Iterator<String> search(Class<T> cls, QueryOptions options)
			throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public <T extends PokeObject> Iterator<String> search(Class<T> cls,
			QueryOptions options, String property) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public int update(String query, Map<String, Object> args) throws GeneralException {
		throw new UnsupportedOperationException();
	}

	public void attach(PokeObject obj) throws GeneralException {
	}

	public void decache(PokeObject obj) throws GeneralException {
	}

	public void decache() throws GeneralException {
	}

	public void clearHighLevelCache() throws GeneralException {
	}

	public <T extends PokeObject> T getUniqueObject(T example) throws GeneralException {
		throw new UnsupportedOperationException();
		/*T object = null;
		QueryOptions ops = new QueryOptions();
		Map<String, String> props = null;
		try {
			props = BeanUtils.describe(example);
		} catch (Exception e) {
			throw new GeneralException(e);
		}
		if (props != null) {
			Iterator<?> it = props.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> ent = (Map.Entry<String, Object>) it.next();
				String name = (String) ent.getKey();
				Object value = ent.getValue();
				if ((name != null) && (!name.equals("class")) && (value != null)) {
					ops.addFilter(Filter.eq(name, value));
				}
			}
		}
		Iterator<?> it = search(example.getClass(), ops);
		if (it.hasNext()) {
			object = (T) it.next();
			if (it.hasNext()) {
				object = null;
			}
		}
		return object;*/
	}

	@SuppressWarnings("unchecked")
	public <T extends PokeObject> T getUniqueObject(
			Class<T> cls, Filter filter) throws GeneralException {
		PokeObject object = null;
		QueryOptions ops = new QueryOptions();
		ops.addFilter(filter);
		Iterator<String> it = search(cls, ops);
		if (it.hasNext()) {
			object = getObjectById(cls, it.next());
			if (it.hasNext()) {
				object = null;
			}
		}
		return (T) object;
	}

	public void enableStatistics(boolean b) {
	}

	public void printStatistics() {
	}

	public void reconnect() {
	}

	public <T extends PokeObject> void removeObjects(
			Class<T> cls, QueryOptions options) throws GeneralException {
		throw new UnsupportedOperationException();
	}
}
