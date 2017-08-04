package net.cattweasel.pokebot.persistence;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.SessionImpl;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import net.cattweasel.pokebot.api.PersistenceManager;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

@SuppressWarnings("deprecation")
public class HibernatePersistenceManager implements PersistenceManager, Cloneable {

	private SessionFactory factory;
	private Session session;
	
	private static final Logger LOG = Logger.getLogger(HibernatePersistenceManager.class);
	
	@Override
	public <T extends PokeObject> T getObject(Class<T> clazz, String idOrName) throws GeneralException {
		T result = getObjectById(clazz, idOrName);
		if (result == null) {
			result = getObjectByName(clazz, idOrName);
		}
		return result;
	}

	@Override
	public Connection getJdbcConnection() throws GeneralException {
		return ((SessionImpl) getSession()).connection();
	}

	@Override
	public void startTransaction() throws GeneralException {
		try {
			Session session = getSession();
			if (session.isOpen()) {
				Transaction t = session.getTransaction();
				if (t.getStatus() != TransactionStatus.ACTIVE) {
					session.beginTransaction();
					session.setFlushMode(FlushMode.COMMIT);
				}
			}
		} catch (HibernateException ex) {
			throw new GeneralException(ex);
		}
	}

	@Override
	public void commitTransaction() throws GeneralException {
		try {
			if (session != null && session.isOpen()) {
				Transaction t = session.getTransaction();
				if (t.getStatus() == TransactionStatus.ACTIVE) {
					t.commit();
				}
				session.beginTransaction();
			}
		} catch (HibernateException ex) {
			throw new GeneralException(ex);
		}
	}

	@Override
	public void rollbackTransaction() throws GeneralException {
		try {
			if (session != null) {
				if (session.isOpen()) {
					Transaction t = session.getTransaction();
					if (t.getStatus() == TransactionStatus.ACTIVE) {
						t.rollback();
					}
				}
				session.close();
				session = null;
			}
		} catch (HibernateException ex) {
			throw new GeneralException(ex);
		}
	}

	@Override
	public void close() throws GeneralException {
		try {
			if (session != null) {
				if (session.isOpen()) {
					Transaction t = session.getTransaction();
					if (t.getStatus() == TransactionStatus.ACTIVE) {
						t.rollback();
					}
				}
				session.close();
				session = null;
			}
		} catch (HibernateException ex) {
			throw new GeneralException(ex);
		}
	}

	@Override
	public <T extends PokeObject> T getObjectById(Class<T> clazz, String id) throws GeneralException {
		T rv = null;
		try {
			startTransaction();
			Session session = getSession();
			rv = (T) clazz.cast(session.get(clazz, id));
		} catch (HibernateException e) {
			throw new GeneralException(e);
		} catch (IllegalArgumentException e) {
			throw new GeneralException(e);
		}
		return rv;
	}

	@Override
	public <T extends PokeObject> T getObjectByName(Class<T> clazz, String name) throws GeneralException {
		Query q;
		try {
			startTransaction();
			Session session = getSession();
			q = getByNameQuery(session, clazz, name);
		} catch (HibernateException e) {
			throw new GeneralException(e);
		}
		T result = getSingleResult(clazz, q);
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void saveObject(PokeObject object) throws GeneralException {
		try {
			startTransaction();
			Session session = getSession();
			if (!session.contains(object)) {
				Class<? extends PokeObject> cls = Hibernate.getClass(object);
				if (object.getId() != null) {
					PokeObject current = (PokeObject) session.createCriteria(cls).add(
							Restrictions.eq("id", object.getId())).uniqueResult();
					if (current != null) {
						session.evict(current);
					}
				}
			}
			session.saveOrUpdate(object);
		} catch (HibernateException e) {
			throw new GeneralException(e);
		}
	}

	@Override
	public void removeObject(PokeObject object) throws GeneralException {
		if (object != null) {
			try {
				startTransaction();
				Session session = getSession();
				session.delete(object);
			} catch (HibernateException e) {
				throw new GeneralException(e);
			}
		}
	}

	@Override
	public <T extends PokeObject> void removeObjects(Class<T> clazz, QueryOptions options) throws GeneralException {
		
		throw new UnsupportedOperationException("not yet implemented"); // TODO
		
	}

	@Override
	public <T extends PokeObject> List<T> getObjects(Class<T> clazz) throws GeneralException {
		return getObjects(clazz, new QueryOptions());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PokeObject> List<T> getObjects(Class<T> clazz, QueryOptions options) throws GeneralException {
		List<T> objects = new ArrayList<T>();
        try {
        		startTransaction();
        		Session session = getSession();
        		Criteria criteria = session.createCriteria(clazz);
        		if (options != null) {
        			mergeCriteriaWithQueryOptions(criteria, options);
        		}
        		objects.addAll(criteria.list());
        } catch (HibernateException ex) {
        		throw new GeneralException(ex);
        }
        return objects;
	}

	@Override
	public <T extends PokeObject> int countObjects(Class<T> clazz) throws GeneralException {
		int count = 0;
		try {
			startTransaction();
			Session session = getSession();
			Criteria criteria = session.createCriteria(clazz);
			Long result = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
			count = result.intValue();
		} catch (HibernateException ex) {
			throw new GeneralException(ex);
		}
		return count;
	}

	@Override
	public <T extends PokeObject> int countObjects(Class<T> clazz, QueryOptions options) throws GeneralException {
		int count = 0;
		try {
			startTransaction();
			Session session = getSession();
			Criteria criteria = session.createCriteria(clazz);
			if (options != null) {
				mergeCriteriaWithQueryOptions(criteria, options);
			}
			Long result = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
			count = result.intValue();
		} catch (HibernateException ex) {
			throw new GeneralException(ex);
		}
		return count;
	}

	@Override
	public void reconnect() {
		try {
			close();
		} catch (Throwable t) {
			LOG.error("Exception during reconnect: " + t.getMessage(), t);
		}
	}

	@Override
	public <T extends PokeObject> Iterator<String> search(Class<T> clazz) throws GeneralException {
		return search(clazz, new QueryOptions());
	}

	@Override
	public <T extends PokeObject> Iterator<String> search(Class<T> clazz, QueryOptions options) throws GeneralException {
		return search(clazz, options, "id");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PokeObject> Iterator<String> search(Class<T> clazz, QueryOptions options, String property)
			throws GeneralException {
		List<String> objects = new ArrayList<String>();
		try {
			startTransaction();
			Session session = getSession();
			Criteria criteria = session.createCriteria(clazz);
			if (options != null) {
				mergeCriteriaWithQueryOptions(criteria, options);
			}
			if (Util.isNotNullOrEmpty(property)) {
				criteria.setProjection(Projections.property(property));
			} else {
				criteria.setProjection(Projections.property("id"));
			}
			List<String> list = criteria.list();
			if (list != null) {
				objects = list;
			}
		} catch (HibernateException ex) {
			throw new GeneralException(ex);
		}
		return objects.iterator();
	}

	@Override
	public <T extends PokeObject> T getUniqueObject(Class<T> clazz, Filter filter) throws GeneralException {
		T result = null;
		QueryOptions qo = new QueryOptions();
		qo.addFilter(filter);
		Iterator<String> it = search(clazz, qo);
		if (it != null && it.hasNext()) {
			String id = it.next();
			if (it.hasNext()) {
				throw new GeneralException("Expected one result!");
			}
			result = getObjectById(clazz, id);
		}
		return result;
	}
	
	@Override
	public Object clone() {
		HibernatePersistenceManager hpm = new HibernatePersistenceManager();
		hpm.setSessionFactory(factory);
		return hpm;
	}
	
	public SessionFactory getSessionFactory() {
		return factory;
	}

	public void setSessionFactory(SessionFactory factory) {
		this.factory = factory;
	}

	public Session getSession() {
		if (session == null) {
			session = factory.openSession();
		}
		return session;
	}
	
	private Query getByNameQuery(Session session, Class<? extends PokeObject> cls, String name) {
		Query q = session.createQuery(String.format("select o from %s o where name = :name", cls.getName()));
		q.setParameter("name", name);
		q.setCacheable(true);
		return q;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends PokeObject> T getSingleResult(Class<T> clazz, Query q) throws GeneralException {
		T rv = null;
		List<T> results = q.list();
		if (null != results && !results.isEmpty()) {
			if (results.size() > 1) {
				throw new GeneralException("Expected one result.");
			}
			rv = (T) results.get(0);
		}
		return rv;
	}
	
	private void mergeCriteriaWithQueryOptions(Criteria criteria, QueryOptions qo) {
		if (qo.getLimit() != 0) {
			criteria.setMaxResults(qo.getLimit());
		}
		if (qo.getFirstResult() != 0) {
			criteria.setFirstResult(qo.getFirstResult());
		}
		if (qo.getOrderProperty() != null && qo.getOrderValue() != null) {
			if (qo.getOrderValue().equalsIgnoreCase("asc")) {
				criteria.addOrder(Order.asc(qo.getOrderProperty()));
			} else {
				criteria.addOrder(Order.desc(qo.getOrderProperty()));
			}
		}
		for (Filter filter : qo.getFilters()) {
			criteria.add(createCriterion(filter));
		}
	}
	
	private static Criterion createCriterion(Filter filter) {
		Criterion result = null;
		switch (filter.getMode()) {
		case EQ:
			result = Expression.eq(filter.getProperty(), filter.getValue());
			break;
		case NE:
			result = Expression.ne(filter.getProperty(), filter.getValue());
			break;
		case LT:
			result = Expression.lt(filter.getProperty(), filter.getValue());
			break;
		case GT:
			result = Expression.gt(filter.getProperty(), filter.getValue());
			break;
		case LIKE:
			if (filter.getValue() instanceof String) {
				result = Expression.like(filter.getProperty(), (String) filter.getValue(), MatchMode.ANYWHERE);
			} else {
				result = Expression.like(filter.getProperty(), filter.getValue());
			}
			break;
		case NOTNULL:
			result = Expression.isNotNull(filter.getProperty());
			break;
		case IN:
			result = Expression.in(filter.getProperty(), (Collection<?>) filter.getValue());
			break;
		case ISNULL:
			result = Expression.isNull(filter.getProperty());
			break;
		case OR:
			List<Criterion> criterions = new ArrayList<Criterion>();
			for (Filter f : filter.getFilters()) {
				criterions.add(createCriterion(f));
			}
			Criterion[] crits = new Criterion[criterions.size()];
			crits = criterions.toArray(crits);
			result = Expression.or(crits);
			break;
		case AND:
			criterions = new ArrayList<Criterion>();
			for (Filter f : filter.getFilters()) {
				criterions.add(createCriterion(f));
			}
			crits = new Criterion[criterions.size()];
			crits = criterions.toArray(crits);
			result = Expression.and(crits);
			break;
		case NOT:
			result = Expression.not(createCriterion(filter.getFilters().get(0)));
			break;
		default:
			throw new RuntimeException("No Handler for Filter-Mode found: " + filter.getMode());
		}
		return result;
	}
}
