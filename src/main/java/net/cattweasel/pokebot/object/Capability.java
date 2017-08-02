package net.cattweasel.pokebot.object;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO implementation for a capability.
 * 
 * @author Benjamin Wesp
 *
 */
@Entity
@Table(name = "db_capability")
@XmlRootElement(name = "Capability")
public class Capability extends PokeObject {

	private static final long serialVersionUID = 3853446081165861447L;
	
	public static final String SYSTEM_ADMINISTRATOR = "SystemAdministrator";
}
