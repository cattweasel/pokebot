package net.cattweasel.pokebot.object;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "db_raid_registration")
@XmlRootElement(name = "RaidRegistration")
public class RaidRegistration extends PokeObject {

	private static final long serialVersionUID = -3773760899066869634L;
}
