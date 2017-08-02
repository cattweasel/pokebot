package net.cattweasel.pokebot.persistence;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import net.cattweasel.pokebot.object.PokeObject;

public class ExtendedEntityInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 5223965164038299922L;

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] currentState,
			String[] propertyNames, Type[] types) throws CallbackException {
		if (entity instanceof PokeObject) {
			for (int i=0; i<propertyNames.length; i++) {
				if ("created".equals(propertyNames[i])) {
					currentState[i] = new Date();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id,
			Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {
		if (entity instanceof PokeObject) {
			for (int i=0; i<propertyNames.length; i++) {
				if ("modified".equals(propertyNames[i])) {
					currentState[i] = new Date();
					return true;
				}
			}
		}
		return false;
	}
}
