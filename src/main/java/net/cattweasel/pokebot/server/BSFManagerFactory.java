package net.cattweasel.pokebot.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.bsf.BSFManager;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.log4j.Logger;

public class BSFManagerFactory extends BaseKeyedPoolableObjectFactory<Object, Object> {

	private Map<String, ManagerStats> _stats;
	private int _maxReuse;

	private static final Logger LOG = Logger.getLogger(BSFManagerFactory.class);

	public BSFManagerFactory() {
		this._stats = new HashMap<String, ManagerStats>();
		this._maxReuse = 100;
	}

	public Object makeObject(Object key) {
		String strKey = checkKey(key);
		ManagerStats stats = new ManagerStats();
		this._stats.put(strKey, stats);
		LOG.debug("New BSFManagerManager Aquired.");
		return new BSFManager();
	}

	public boolean validateObject(Object key, Object obj) {
		String strKey = checkKey(key);
		boolean valid = super.validateObject(key, obj);
		if (valid) {
			ManagerStats stats = (ManagerStats) this._stats.get(strKey);
			if (stats == null) {
				valid = false;
			}
		}
		return valid;
	}

	public void destroyObject(Object key, Object obj) throws Exception {
		String strKey = checkKey(key);
		this._stats.remove(strKey);
		if (obj != null && obj instanceof BSFManager) {
			BSFManager manager = (BSFManager) obj;
			manager.terminate();
		}
	}

	public void activateObject(Object key, Object obj) {
		String strKey = checkKey(key);
		ManagerStats stats = (ManagerStats) this._stats.get(key);
		if (stats != null) {
			stats.incrementUse();
			this._stats.put(strKey, stats);
		}
	}

	private String checkKey(Object key) throws IllegalArgumentException {
		if (key == null) {
			throw new IllegalArgumentException("Pool key cannot be null.");
		}
		if (!(key instanceof String)) {
			throw new IllegalArgumentException("Pool key must be a String.");
		}
		String strKey = (String) key;
		if (strKey.length() == 0)
			throw new IllegalArgumentException("Pool key must have a length greater then zero.");
		return strKey;
	}

	public int getMaxManagerReuse() {
		return this._maxReuse;
	}

	public void setMaxManagerReuse(int reuse) {
		this._maxReuse = reuse;
	}

	public static class ManagerStats {

		int _numUses;

		public ManagerStats() {
			this._numUses = 1;
		}

		public void incrementUse() {
			this._numUses += 1;
		}

		public int getNumUses() {
			return this._numUses;
		}
	}
}
