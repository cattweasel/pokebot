package net.cattweasel.pokebot.object;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "db_player")
@XmlRootElement(name = "Player")
public class Player extends PokeObject {

	private static final long serialVersionUID = -6224787576181683935L;
}
