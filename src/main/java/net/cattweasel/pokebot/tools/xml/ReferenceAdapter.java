package net.cattweasel.pokebot.tools.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.Reference;
import net.cattweasel.pokebot.tools.GeneralException;

/**
 * XmlAdapter for converting between references and their original value.
 * 
 * @author Benjamin Wesp
 *
 */
public class ReferenceAdapter extends XmlAdapter<Reference, PokeObject> {

	private PokeContext context;
	
	private static final Logger LOG = Logger.getLogger(ReferenceAdapter.class);

	/**
	 * Converts an object into a reference.
	 * 
	 * @param object The object to be converted
	 * @return The converted object
	 * @throws Exception In case of any error
	 */
    @Override
    public Reference marshal(PokeObject object) throws Exception {
    		Reference ref = object == null ? null : new Reference(object);
    		return ref;
    }

    /**
     * Converts a reference into an object.
     * 
     * @param ref The reference to be converted
     * @return The converted reference
     * @throws Exception In case of any error
     */
    @Override
    @SuppressWarnings("unchecked")
    public PokeObject unmarshal(Reference ref) throws Exception {
	    	PokeObject obj = null;
	    	try {
	    		Class<PokeObject> clazz = (Class<PokeObject>) Class.forName(ref.getClazz());
	    		if (ref.getId() != null) {
	    			obj = getContext().getObjectById(clazz, ref.getId());
	    		} else {
	    			obj = getContext().getObjectByName(clazz, ref.getName());
	    		}
	    	} catch (Exception ex) {
	    		LOG.error(ex);
	    	}
	    	return obj;
    }
    
    /**
     * Private helper method to retrieve the current {@link net.cattweasel.pokebot.api.TWContext}.
     * 
     * @return The current {@link net.cattweasel.pokebot.api.TWContext}
     */
    private PokeContext getContext() {
	    	if (context == null) {
	    		try {
	    			context = PokeFactory.getCurrentContext();
	    		} catch (GeneralException ex) {
	    			LOG.error(ex);
	    		}
	    	}
	    	return context;
    }
}
