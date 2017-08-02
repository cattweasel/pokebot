package net.cattweasel.pokebot.tools.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.Wrapper;

/**
 * XmlAdapter for converting between wrapped references and their original value.
 * 
 * @author Benjamin Wesp
 *
 */
public class WrappedReferenceAdapter extends XmlAdapter<Wrapper, PokeObject> {

	/**
	 * Converts an object into a wrapped reference.
	 * 
	 * @param object The object to be converted
	 * @return The converted object
	 * @throws Exception In case of any error
	 */
	@Override
	public Wrapper marshal(PokeObject object) throws Exception {
		return object == null ? null : new Wrapper(object);
	}
	
	/**
     * Converts a wrapped reference into an object.
     * 
     * @param wrapper The wrapped reference to be converted
     * @return The converted reference
     * @throws Exception In case of any error
     */
	@Override
	public PokeObject unmarshal(Wrapper wrapper) throws Exception {
		return new ReferenceAdapter().unmarshal(wrapper.getObject());
	}
}
