package net.cattweasel.pokebot.api;

import java.util.List;

import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.tools.GeneralException;

/**
 * The interface of an object capable of resolving references to PokeObjects.
 * 
 * Used by a few classes that normally references by name, but provide
 * convenience methods to resolve the object.
 * 
 * @author Benjamin Wesp
 *
 */
public interface Resolver {

	/**
	 * Retrieve an object by id.
	 * 
	 * @param clazz The class of the object to be retrieved
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param id The id of the object to be retrieved
	 * @return The object if found - null otherwise
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> T getObjectById(Class<T> clazz, String id) throws GeneralException;

	/**
	 * Retrieve an object by name.
	 * 
	 * @param clazz The class of the object to be retrieved
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param name The name of the object to be retrieved
	 * @return The object if found - null otherwise
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> T getObjectByName(Class<T> clazz, String name) throws GeneralException;

	/**
	 * Retrieve an object by id or name.
	 * 
	 * @param clazz The class of the object to be retrieved
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param idOrName The id or name of the object to be retrieved
	 * @return The object if found - null otherwise
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> T getObject(Class<T> clazz, String idOrName) throws GeneralException;

	/**
	 * Get a list of objects matching the query options.
	 * 
	 * @param clazz The class of the objects to be retrieved
	 * @param <T> A class which extends @{link net.cattweasel.pokebot.object.PokeObject}
	 * @param options The query options to be used
	 * @return A list of objects
	 * @throws GeneralException In case of any error
	 */
	<T extends PokeObject> List<T> getObjects(Class<T> clazz, QueryOptions options) throws GeneralException;
}
