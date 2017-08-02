package net.cattweasel.pokebot.server;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.RuleRunner;
import net.cattweasel.pokebot.object.Rule;
import net.cattweasel.pokebot.tools.GeneralException;

public class BSFRuleRunner implements RuleRunner {

	private GenericKeyedObjectPool<Object, Object> _pool;

	private static final Logger LOG = Logger.getLogger(BSFRuleRunner.class);

	public BSFRuleRunner() {
	}

	private BSFManager getBSFManager(Rule rule) throws GeneralException {
		BSFManager manager = null;
		if (this._pool != null) {
			try {
				String poolId = getPoolKey(rule);
				manager = (BSFManager) this._pool.borrowObject(poolId);
			} catch (Exception e) {
				throw new GeneralException("Error getting BSFManager from pool." + e.toString());
			}
		} else {
			manager = new BSFManager();
		}
		return manager;
	}

	public Object runRule(Rule rule, Map<String, Object> params) throws GeneralException {
		return runRule(rule, params, null);
	}

	public Object runRule(Rule rule, Map<String, Object> params, List<Rule> ruleLibraries) throws GeneralException {
		LOG.debug("*** Running rule: " + rule.getName() + " ***");
		BSFManager manager = getBSFManager(rule);
		Object returnVal = null;
		try {
			bindAttributes(manager, params);
			if (ruleLibraries != null && !ruleLibraries.isEmpty()) {
				for (Rule ruleLibrary : ruleLibraries) {
					eval(manager, ruleLibrary);
				}
			}
			List<Rule> referenced = rule.getReferencedRules();
			if (null != referenced) {
				for (Rule ref : referenced) {
					eval(manager, ref);
				}
			}
			returnVal = eval(manager, rule);
		} catch (BSFException bsfe) {
			throw new GeneralException(bsfe);
		} catch (Throwable t) {
			throw new GeneralException(t);
		} finally {
			unbindAttributes(manager, params);
			if (this._pool != null) {
				try {
					String key = getPoolKey(rule);
					this._pool.returnObject(key, manager);
				} catch (Exception e) {
					LOG.warn("Exception returning manager to pool." + e.toString());
				}
			} else if (manager != null) {
				manager.terminate();
				manager = null;
			}
		}
		return returnVal;
	}

	private Object eval(BSFManager manager, Rule rule) throws BSFException {
		LOG.debug("Evaluating rule source: " + rule.getName());
		LOG.debug(rule.getSource());
		return manager.eval(rule.getLanguage(), rule.getName(), 0, 0, rule.getSource());
	}

	private void bindAttributes(BSFManager manager, Map<String, Object> params) throws BSFException {
		if (null != params) {
			for (Entry<String, Object> entry : params.entrySet()) {
				String name = entry.getKey();
				if (name != null) {
					Object value = params.get(name);
					Class<?> type = null != value ? value.getClass() : Object.class;
					if (!name.contains(".")) {
						manager.declareBean(name, value, type);
					} else {
						LOG.debug("Not declaring param (" + name + ") as a bean"
								+ " because it contains a dot '.' in the name.");
					}
				}
			}
		}
	}

	private void unbindAttributes(BSFManager manager, Map<String, Object> params) {
		if (null != params) {
			try {
				for (String name : params.keySet()) {
					if (name != null) {
						manager.undeclareBean(name);
					}
				}
			} catch (BSFException e) {
				LOG.warn("Problem unbinding rule attribute:" + e.toString());
			}
		}
	}

	private String getPoolKey(Rule rule) {
		String key = rule.getName();
		if (key == null) {
			key = rule.getId();
		}
		if (key == null) {
			LOG.warn("Both rule Name and id are null going postal and using the hashCode of the source.");
			String source = rule.getSource();
			int hashCode = source.hashCode();
			key = String.valueOf(hashCode);
		}
		return key;
	}

	public void setBSFManagerPool(GenericKeyedObjectPool<Object, Object> pool) {
		this._pool = pool;
	}

	public GenericKeyedObjectPool<Object, Object> getBSFManagerPool() {
		return this._pool;
	}
}
