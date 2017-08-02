package net.cattweasel.pokebot.api;

/**
 * The primary API for accessing the persistence store and performing core system operations.
 * 
 * @author Benjamin Wesp
 *
 */
public abstract interface PokeContext extends PersistenceManager, RuleRunner {

	/**
	 * Return a context derived from this one.
	 * 
	 * @return A derived context instance
	 */
	PokeContext getContext();

	/**
	 * Set the name of the current user of this context.
	 * 
	 * @param username The username to be set
	 */
	void setUsername(String username);

	/**
	 * Returns the name of the current user of this context.
	 * 
	 * @return The username of the current context
	 */
	String getUsername();

	/**
	 * Sets a property in the current context instance.
	 * 
	 * @param key The key of the property
	 * @param value The value of the property
	 */
	void setProperty(String key, Object value);

	/**
	 * Returns a property of the current context instance.
	 * 
	 * @param key The key of the property
	 * @return The property if found - null otherwise
	 */
	Object getProperty(String key);
}
