package net.cattweasel.pokebot.persistence;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.cattweasel.pokebot.api.PersistenceManager;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.tools.GeneralException;

public class ClassPersistenceManager implements PersistenceManager, Cloneable {

	private Map<Class<?>, PersistenceManager> mManagers;
	private PersistenceManager mDefault;

	public ClassPersistenceManager() {
		this.mManagers = new HashMap<Class<?>, PersistenceManager>();
	}
	
	public Connection getJdbcConnection() throws GeneralException {
		return this.mDefault.getJdbcConnection();
	}

	public void addManager(Class<?> cls, PersistenceManager pm) {
		this.mManagers.put(cls, pm);
	}

	public void setDefaultManager(PersistenceManager pm) {
		this.mDefault = pm;
	}

	@SuppressWarnings("unchecked")
	public void setManagers(Map<Object, Object> src) throws GeneralException {
		if (src != null) {
			Iterator<?> it = src.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Object, Object> ent = (Map.Entry<Object, Object>) it.next();
				Object key = ent.getKey();
				PersistenceManager man = (PersistenceManager) ent.getValue();
				if (key instanceof Class) {
					addManager((Class<?>) key, man);
				} else if (key != null) {
					try {
						String cname = key.toString();
						if (cname.indexOf(".") < 0)
							cname = "net.cattweasel.pokebot.object." + cname;
						Class<?> cls = Class.forName(cname);
						addManager(cls, man);
					} catch (ClassNotFoundException e) {
						throw new GeneralException(e);
					}
				}
			}
		}
	}

	public PersistenceManager getManager(Class<?> cls) throws GeneralException {
		PersistenceManager pm = (PersistenceManager) this.mManagers.get(cls);
		if (pm == null) {
			pm = this.mDefault;
			if (pm == null)
				throw new GeneralException("No persistence handler for class: " + cls.getName());
		}
		return pm;
	}

	public PersistenceManager getManager(PokeObject obj) throws GeneralException {
		return getManager(obj.getClass());
	}

	public Object clone() throws CloneNotSupportedException {
		ClassPersistenceManager cpm = new ClassPersistenceManager();
		if (null != this.mDefault) {
			cpm.mDefault = (PersistenceManager) ((Cloneable) this.mDefault.clone());
		}
		if (null != this.mManagers) {
			for (Map.Entry<Class<?>, PersistenceManager> entry : this.mManagers.entrySet()) {
				cpm.addManager((Class<?>) entry.getKey(),
						(PersistenceManager) ((PersistenceManager) entry.getValue()).clone());
			}
		}
		return cpm;
	}

	public void startTransaction() throws GeneralException {
		this.mDefault.startTransaction();
		for (PersistenceManager pm : this.mManagers.values()) {
			pm.startTransaction();
		}
	}

	public void commitTransaction() throws GeneralException {
		this.mDefault.commitTransaction();
		for (PersistenceManager pm : this.mManagers.values()) {
			pm.commitTransaction();
		}
	}

	public void rollbackTransaction() throws GeneralException {
		this.mDefault.rollbackTransaction();
		for (PersistenceManager pm : this.mManagers.values()) {
			pm.rollbackTransaction();
		}
	}

	public void close() throws GeneralException {
		this.mDefault.close();
		for (PersistenceManager pm : this.mManagers.values()) {
			pm.close();
		}
	}

	public <T extends PokeObject> T getObjectById(Class<T> cls, String id) throws GeneralException {
		return getManager(cls).getObjectById(cls, id);
	}

	public <T extends PokeObject> T getObjectByName(Class<T> cls, String name) throws GeneralException {
		return getManager(cls).getObjectByName(cls, name);
	}

	public <T extends PokeObject> T getObject(Class<T> cls, String idOrName) throws GeneralException {
		return getManager(cls).getObject(cls, idOrName);
	}

	public void saveObject(PokeObject obj) throws GeneralException {
		getManager(obj).saveObject(obj);
	}

	public void removeObject(PokeObject obj) throws GeneralException {
		getManager(obj).removeObject(obj);
	}

	public <T extends PokeObject> void removeObjects(Class<T> cls, QueryOptions options) throws GeneralException {
		getManager(cls).removeObjects(cls, options);
	}

	public <T extends PokeObject> List<T> getObjects(Class<T> cls) throws GeneralException {
		return getManager(cls).getObjects(cls);
	}

	public <T extends PokeObject> List<T> getObjects(Class<T> cls, QueryOptions options) throws GeneralException {
		return getManager(cls).getObjects(cls, options);
	}

	public <T extends PokeObject> Iterator<String> search(Class<T> cls) throws GeneralException {
		return getManager(cls).search(cls);
	}
	
	public <T extends PokeObject> Iterator<String> search(Class<T> cls, QueryOptions options) throws GeneralException {
		return getManager(cls).search(cls, options);
	}

	public <T extends PokeObject> Iterator<String> search(Class<T> cls, QueryOptions options, String properties)
			throws GeneralException {
		return getManager(cls).search(cls, options, properties);
	}

	public <T extends PokeObject> int countObjects(Class<T> cls, QueryOptions options) throws GeneralException {
		return getManager(cls).countObjects(cls, options);
	}

	public <T extends PokeObject> T getUniqueObject(Class<T> cls, Filter filter) throws GeneralException {
		return getManager(cls).getUniqueObject(cls, filter);
	}

	public void reconnect() {
		this.mDefault.reconnect();
		for (PersistenceManager pm : this.mManagers.values()) {
			pm.reconnect();
		}
	}

	@Override
	public <T extends PokeObject> int countObjects(Class<T> clazz) throws GeneralException {
		return getManager(clazz).countObjects(clazz);
	}
}
