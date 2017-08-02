package net.cattweasel.pokebot.api;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.tools.GeneralException;

/**
 * Interface implemented by classes that provide persistence services.
 * 
 * @author Benjamin Wesp
 *
 */
public abstract interface PersistenceManager extends Resolver {

	/**
	 * Returns the underlying JDBC connection of this persistence manager.
	 * 
	 * @return The resulting JDBC Connection
	 * @throws GeneralException In case of any error
	 */
	Connection getJdbcConnection() throws GeneralException;
	
	/**
	 * Optional clone method.
	 * 
	 * @return A clone of this persistence manager
	 * @throws CloneNotSupportedException If this persistence manager cannot be cloned
	 */
	Object clone() throws CloneNotSupportedException;

	/**
	 * Begin a new transaction.
	 * 
	 * @throws GeneralException In case of any error
	 */
	void startTransaction() throws GeneralException;

	/**
	 * Commit the current transaction.
	 * 
	 * @throws GeneralException In case of any error
	 */
	void commitTransaction() throws GeneralException;

	/**
	 * Roll back the current transaction.
	 * 
	 * @throws GeneralException In case of any error
	 */
	void rollbackTransaction() throws GeneralException;

	/**
	 * Close this persistent manager and release any resources in use.
	 * 
	 * @throws GeneralException In case of any error
	 */
	void close() throws GeneralException;

	/**
	 * Retrieve an object by its id.
	 * 
	 * @param clazz The class of the object to be retrieved
	 * @param id The id of the object
	 * @return The object of found - null otherwise
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> T getObjectById(Class<T> clazz, String id) throws GeneralException;

	/**
	 * Retrieve an object by its name.
	 * 
	 * @param clazz The class of the object to be retrieved
	 * @param name The name of the object
	 * @return The object if found - null otherwise
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> T getObjectByName(Class<T> clazz, String name) throws GeneralException;

	/**
	 * Save an object to the persistent store.
	 * 
	 * @param object The object to be saved
	 * @throws GeneralException In case of any error
	 */
	void saveObject(PokeObject object) throws GeneralException;

	/**
	 * Remove an object from the persistent store.
	 * 
	 * @param object The object to be removed
	 * @throws GeneralException In case of any error
	 */
	void removeObject(PokeObject object) throws GeneralException;

	/**
	 * Bulk remove objects based on the filters defined in the query options.
	 * 
	 * @param clazz The class of the objects to be removed
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param options The query options to be applied
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> void removeObjects(Class<T> clazz, QueryOptions options) throws GeneralException;

	/**
	 * Return all objects of a given class.
	 * 
	 * @param clazz The class of the objects to be returned
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @return A list of objects found
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> List<T> getObjects(Class<T> clazz) throws GeneralException;
	
	/**
	 * Return all objects of a given class, constrained by filters in the query options.
	 * 
	 * @param clazz The class of the objects to be returned
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param options The query options to be applied
	 * @return A list of objects found
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> List<T> getObjects(Class<T> clazz, QueryOptions options) throws GeneralException;

	/**
	 * Count the number of objects.
	 * 
	 * @param clazz The class of objects to be counted
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @return The number of objects found
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> int countObjects(Class<T> clazz) throws GeneralException;
	
	/**
	 * Count the number of objects that match a criteria.
	 * 
	 * @param clazz The class of objects to be counted
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param options The query options to be applied
	 * @return The number of objects found
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> int countObjects(Class<T> clazz, QueryOptions options) throws GeneralException;

	/**
	 * Reconnect the persistence manager to the persistent store.
	 */
	void reconnect();

	/**
	 * Perform a projection search and return an iterator.
	 * 
	 * @param clazz The class of the objects to search for
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @return An iterator containing the IDs of the objects
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> Iterator<String> search(Class<T> clazz) throws GeneralException;
	
	/**
	 * Perform a projection search and return an iterator.
	 * 
	 * @param clazz The class of the objects to search for
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param options The query options to be applied
	 * @return An iterator containing the IDs of the objects
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> Iterator<String> search(Class<T> clazz, QueryOptions options) throws GeneralException;

	/**
	 * Perform a projection search for a selected object property and return an iterator.
	 * 
	 * @param clazz The class of the objects to search for
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param options The query options to be applied
	 * @param property The property to be returned
	 * @return An iterator containing the selected property
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> Iterator<String> search(Class<T> clazz,
			QueryOptions options, String property) throws GeneralException;
	
	/**
	 * Retrieve an object matching a filter.
	 * 
	 * @param clazz The class of the object to be retrieved
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param filter The filter to lookup
	 * @return The object if found - null otherwise
	 * @throws GeneralException In case of any error or if there are more than one object
	 */
	<T extends PokeObject> T getUniqueObject(Class<T> clazz, Filter filter) throws GeneralException;
}
