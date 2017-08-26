package net.cattweasel.pokebot.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.tools.GeneralException;

/**
 * Factory service for TWContexts.
 * 
 * The TWContext is the primary API that most of the system code should use to
 * access core services like the persistent store, task scheduler and the email
 * notifier. A TWContext must be obtained by calling this factory which knows how
 * to create them from a prototype instance injected by Spring.
 * 
 * @author Benjamin Wesp
 *
 */
public class PokeFactory {

	private static PokeFactory mSingleton;
	private PokeContext _prototype;
	
	private static ThreadLocal<PokeContext> contexts = new ThreadLocal<PokeContext>();
	
	private static final Logger LOG = Logger.getLogger(PokeFactory.class);

	public static PokeFactory getFactory() {
		synchronized (PokeFactory.class) {
			if (mSingleton == null) {
				mSingleton = new PokeFactory();
			}
		}
		return mSingleton;
	}

	public void setContextPrototype(PokeContext c) {
		this._prototype = c;
	}

	public PokeContext getContextPrototype() {
		return this._prototype;
	}

	/**
	 * Creates a new context and install it in thread local storage.
	 * 
	 * @return A new context instance for this thread
	 * @throws GeneralException If there was a context created previously
	 */
	public static PokeContext createContext() throws GeneralException {
		getFactory();
		PokeContext con = (PokeContext) contexts.get();
		if (null == con) {
			con = createPrivateContext();
			contexts.set(con);
		} else {
			throw new GeneralException("Context already created for this thread!");
		}
		return con;
	}

	/**
	 * Temporarily switch to a new context while preserving the old one.
	 * 
	 * @return The new context instance
	 * @throws GeneralException In case of any error
	 */
	public static PokeContext pushContext() throws GeneralException {
		PokeContext current = (PokeContext) contexts.get();
		PokeContext con = createPrivateContext();
		contexts.set(con);
		return current;
	}

	public static void popContext(PokeContext prev) throws GeneralException {
		PokeContext context = (PokeContext) contexts.get();
		if (null != context) {
			try {
				context.close();
			} catch (GeneralException ex) {
				LOG.error("Could not close context from stack: " + ex.getMessage(), ex);
			}
		}
		contexts.set(prev);
	}

	public static void restoreContext(PokeContext con) throws GeneralException {
		contexts.set(con);
	}

	public static PokeContext createContext(String user) throws GeneralException {
		PokeContext con = createContext();
		if (con != null) {
			con.setUsername(user);
		}
		return con;
	}

	public static PokeContext createPrivateContext() throws GeneralException {
		PokeFactory factory = getFactory();
		PokeContext proto = factory.getContextPrototype();
		if (proto == null) {
			throw new GeneralException("No prototype context exists");
		}
		PokeContext ctx = proto.getContext();
		contextCreated(ctx);
		return ctx;
	}

	public static void releasePrivateContext(PokeContext context) throws GeneralException {
		if (null != context) {
			try {
				context.close();
			} catch (GeneralException ex) {
				LOG.error("Could not close private context: " + ex.getMessage(), ex);
			}
		}
	}

	public static PokeContext getCurrentContext() throws GeneralException {
		getFactory();
		PokeContext con = (PokeContext) contexts.get();
		if (null == con) {
			throw new GeneralException("Context not available in this thread!");
		}
		return con;
	}

	public static PokeContext peekCurrentContext() {
		getFactory();
		PokeContext con = (PokeContext) contexts.get();
		return con;
	}

	public static void releaseContext(PokeContext context, boolean publishMeters) throws GeneralException {
		if (null != context) {
			try {
				context.close();
			} catch (GeneralException ex) {
				LOG.error("Could not close context: " + ex.getMessage(), ex);
			}
		}
		contexts.set(null);
	}

	public static void releaseContext(PokeContext context) throws GeneralException {
		releaseContext(context, true);
	}

	public static void releaseContextNoMeters(PokeContext context) throws GeneralException {
		releaseContext(context, false);
	}

	public static void setContext(PokeContext ctx) {
		contexts.set(ctx);
	}

	private static void contextCreated(PokeContext ctx) {
		if (null != ctx) {
			Throwable t = new Throwable();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
		}
	}
}
