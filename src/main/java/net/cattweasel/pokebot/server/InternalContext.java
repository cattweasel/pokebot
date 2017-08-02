package net.cattweasel.pokebot.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PersistenceManager;
import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.RuleRunner;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.Rule;
import net.cattweasel.pokebot.tools.GeneralException;

public class InternalContext extends AbstractContext {

	private Environment _env;
	private PersistenceManager _store;
	private Connection _dbCon;
	private boolean _closed;
	private String _userName;

	private static final Logger LOG = Logger.getLogger(InternalContext.class);

	public InternalContext() {
	}
	
	public void setEnvironment(Environment env) {
		this._env = env;
	}

	public Environment getEnvironment() {
		return this._env;
	}

	public void setPersistenceManager(PersistenceManager pm) {
		this._store = pm;
	}

	public PersistenceManager getPersistenceManager() {
		return this._store;
	}

	public PokeContext getContext() {
		InternalContext ctx = new InternalContext();
		ctx.setEnvironment(this._env);
		PersistenceManager pm = this._env.getPersistenceManager();
		if (pm instanceof Cloneable) {
			try {
				pm = (PersistenceManager) pm.clone();
			} catch (CloneNotSupportedException e) {
			}
		}
		ctx.setPersistenceManager(pm);
		return ctx;
	}

	public Connection getJdbcConnection() throws GeneralException {
		DataSource ds = this._env.getSpringDataSource();
		if (ds == null) {
			throw new GeneralException("Unable to return connection, no DataSource defined!");
		}
		try {
			if (this._dbCon == null)
				this._dbCon = ds.getConnection();
		} catch (SQLException se) {
			throw new GeneralException(se);
		}
		return this._dbCon;
	}

	public boolean isClosed() {
		return this._closed;
	}

	public void setUsername(String name) {
		this._userName = name;
	}

	public String getUsername() {
		return this._userName;
	}

	public void startTransaction() throws GeneralException {
		this._store.startTransaction();
	}

	public void commitTransaction() throws GeneralException {
		this._store.commitTransaction();
	}

	public void rollbackTransaction() throws GeneralException {
		this._store.rollbackTransaction();
	}

	public void close() throws GeneralException {
		if (this._store != null) {
			this._store.close();
		}
		if (this._dbCon != null) {
			try {
				this._dbCon.close();
			} catch (SQLException ex) {
				LOG.debug("Exception closing db connection:" + ex.toString());
			}
		}
		this._closed = true;
	}

	public <T extends PokeObject> T getObject(Class<T> cls, String nameOrId) throws GeneralException {
		T obj = null;
		if (nameOrId != null) {
			obj = getObjectById(cls, nameOrId);
		}
		if (obj == null && nameOrId != null) {
			obj = getObjectByName(cls, nameOrId);
		}
		return obj;
	}

	public <T extends PokeObject> T getObjectById(Class<T> cls, String id) throws GeneralException {
		return this._store.getObjectById(cls, id);
	}
	
	public <T extends PokeObject> T getUniqueObject(Class<T> cls, Filter filter) throws GeneralException {
		return this._store.getUniqueObject(cls, filter);
	}

	public <T extends PokeObject> T getObjectByName(Class<T> c, String name) throws GeneralException {
		return this._store.getObjectByName(c, name);
	}

	public void saveObject(PokeObject obj) throws GeneralException {
		this._store.saveObject(obj);
	}

	public void removeObject(PokeObject obj) throws GeneralException {
		this._store.removeObject(obj);
	}

	public <T extends PokeObject> void removeObjects(Class<T> cls, QueryOptions options) throws GeneralException {
		this._store.removeObjects(cls, options);
	}

	public <T extends PokeObject> List<T> getObjects(Class<T> cls) throws GeneralException {
		return getObjects(cls, null);
	}

	public <T extends PokeObject> List<T> getObjects(Class<T> cls, QueryOptions options) throws GeneralException {
		return this._store.getObjects(cls, options);
	}

	public <T extends PokeObject> int countObjects(Class<T> cls) throws GeneralException {
		return this._store.countObjects(cls);
	}
	
	public <T extends PokeObject> int countObjects(Class<T> cls, QueryOptions ops) throws GeneralException {
		return this._store.countObjects(cls, ops);
	}

	public void reconnect() {
		this._store.reconnect();
	}

	public <T extends PokeObject> Iterator<String> search(Class<T> cls) throws GeneralException {
		return this._store.search(cls);
	}
	
	public <T extends PokeObject> Iterator<String> search(Class<T> cls, QueryOptions options) throws GeneralException {
		return this._store.search(cls, options);
	}

	public <T extends PokeObject> Iterator<String> search(Class<T> cls, QueryOptions options, String property) throws GeneralException {
		return this._store.search(cls, options, property);
	}
	
	public Object runRule(Rule rule, Map<String, Object> params) throws GeneralException {
		return runRule(rule, params, null);
	}

	public Object runRule(Rule rule, Map<String, Object> params, List<Rule> libraries) throws GeneralException {
		Object rv = null;
		RuleRunner rr = this._env.getRuleRunner();
		if (rr != null) {
			if (params == null) {
				params = new HashMap<String, Object>();
			}
			if (params.get("context") == null) {
				params.put("context", this);
			}
			if (params.get("log") == null) {
				params.put("log", LOG);
			}
			rv = rr.runRule(rule, params, libraries);
		}
		return rv;
	}
}
