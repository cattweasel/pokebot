package net.cattweasel.pokebot.object;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.DateAdapter;

@Entity
@Table(name = "db_user_notification")
@XmlRootElement(name = "UserNotification")
public class UserNotification extends PokeObject {

	private static final long serialVersionUID = 3736536634487658349L;
	
	private Date expiration;
	private String messageId;

	@Column(unique = false, nullable = false)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
	
	@Column(unique = true, nullable = false)
	@XmlAttribute
	public String getMessageId() {
		return messageId;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
}
