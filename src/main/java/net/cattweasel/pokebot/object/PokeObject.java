package net.cattweasel.pokebot.object;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.json.simple.JSONObject;

import net.cattweasel.pokebot.tools.xml.AbstractXmlObject;
import net.cattweasel.pokebot.tools.xml.DateAdapter;

/**
 * POJO superclass for all API related objects.
 * 
 * @author Benjamin Wesp
 *
 */
@MappedSuperclass
public class PokeObject extends AbstractXmlObject implements Serializable {

	private static final long serialVersionUID = 7976793253699960999L;
	
	private String id;
	private String name;
	private Date created;
	private Date modified;
	
	private static final Logger LOG = Logger.getLogger(PokeObject.class);

	/**
	 * Returns the internal ID of this object.
	 * 
	 * @return The internal ID of this object
	 */
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "uuid", unique = true, length = 36)
	@XmlAttribute
	public String getId() {
		return id;
	}

	/**
	 * Sets the internal ID for this object.
	 * 
	 * @param id The internal ID for this object
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the internal name of this object.
	 * 
	 * @return The internal name of this object
	 */
	@Column(unique = true, nullable = false, length = 128)
	@XmlAttribute
	public String getName() {
		return name;
	}

	/**
	 * Sets the internal name for this object.
	 * 
	 * @param name The internal name for this object
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the date when this object was created.
	 * 
	 * @return The date when this object was created
	 */
	@Column(unique = false, nullable = false)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getCreated() {
		return created == null ? null : new Date(created.getTime());
	}

	/**
	 * Sets the date when this object was created.
	 * 
	 * @param created The date when this object was created
	 */
	public void setCreated(Date created) {
		this.created = created == null ? null : new Date(created.getTime());
	}

	/**
	 * Returns the date when this object was modified the last time.
	 * 
	 * @return The date when this object was modified the last time
	 */
	@Column(unique = false, nullable = true)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getModified() {
		return modified == null ? null : new Date(modified.getTime());
	}

	/**
	 * Sets the date when this object was modified the last time.
	 * 
	 * @param modified The date when this object was modified the last time
	 */
	public void setModified(Date modified) {
		this.modified = modified == null ? null : new Date(modified.getTime());
	}
	
	/**
	 * Overriding the default toString method for a nicer view.
	 * 
	 * @return The formated string to display
	 */
	@Override
	public String toString() {
		return String.format("%s[id = %s, name = %s]", getClass().getSimpleName(), getId(), getName());
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		Map<String, Object> attributes = getObjectAttributes(this);
		JSONObject json = new JSONObject();
		for (Entry<String, Object> entry : attributes.entrySet()) {
			Object attrValue = entry.getValue();
			if (attrValue instanceof List) {
				List<Object> list = new ArrayList<Object>();
				for (Object o : ((List<Object>) attrValue)) {
					list.add(getJSONValue(o));
				}
				attrValue = list;
			} else {
				attrValue = getJSONValue(attrValue);
			}
			json.put(entry.getKey(), attrValue);
		}
		return json;
    }
	
	private Object getJSONValue(Object o) {
		if (o instanceof PokeObject) {
			o = ((PokeObject) o).toJson();
		} else if (o instanceof Date) {
			o = ((Date) o).getTime();
		} else if (o instanceof Class) {
			o = ((Class<?>) o).getName();
		}
		return o;
	}
	
	private <T extends PokeObject> Map<String, Object> getObjectAttributes(T object) {
        Map<String, Object> attrs = new HashMap<String, Object>();
        for (Method method : object.getClass().getMethods()) {
	        	/*boolean isReference = false;
	    		for (Annotation a : method.getAnnotations()) {
	    			Class<? extends Annotation> clazz = a.annotationType();
	    			if (clazz == XmlJavaTypeAdapter.class) {
	    				XmlJavaTypeAdapter adapter = (XmlJavaTypeAdapter) a;
	    				if (adapter.value() == ReferenceAdapter.class
	    						|| adapter.value() == WrappedReferenceAdapter.class) {
	    					isReference = true;
	    				}
	    			}
	    		}*/
	    		//if (!isReference) {
	    			resolveMethodAttribute(object, attrs, method);
	    		//}
        }
        return attrs;
    }
	
	private <T extends PokeObject> void resolveMethodAttribute(
			T object, Map<String, Object> attrs, Method method) {
		if (method.getName().startsWith("to")
	    			|| method.getName().equals("wait")
	    			|| method.getName().equals("hashCode")
	    			|| method.getParameterTypes().length != 0
	    			|| method.getName().equals("notify")
	    			|| method.getName().equals("notifyAll")
	    			|| method.getName().contains("Hibernate")
	    			|| method.getName().contains("TracingAspect")) {
	    		return;
		}
	    	int subIdx = method.getName().startsWith("is") ? 2 : 3;
	    	String attrName = method.getName().substring(subIdx, (subIdx + 1)).toLowerCase() 
					+ method.getName().substring((subIdx + 1), method.getName().length());
	    	Object attrVal = null;
	    	try {
	    		attrVal = method.invoke(object);
	    		if (attrVal instanceof Enum) {
	    			attrVal = ((Enum<?>) attrVal).name();
	    		}
	    		attrs.put(attrName, attrVal);
	    	} catch (Exception ex) {
	    		LOG.error(ex);
	    	}
	}
}
