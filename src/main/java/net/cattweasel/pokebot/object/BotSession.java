package net.cattweasel.pokebot.object;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.WrappedReferenceAdapter;

@Entity
@Table(name = "db_bot_session", uniqueConstraints = { @UniqueConstraint(columnNames = "chatId") })
@XmlRootElement(name = "BotSession")
public class BotSession extends PokeObject {

	private static final long serialVersionUID = 8299634003354015640L;
	
	private Long chatId;
	private User user;
	private Attributes<String, Object> attributes;
	
	@Column(unique = true, nullable = false)
	@XmlAttribute
	public Long getChatId() {
		return chatId;
	}
	
	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}

	@ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "user", unique = true, nullable = false)
	@XmlElement(name = "User")
	@XmlJavaTypeAdapter(WrappedReferenceAdapter.class)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Lob
	@Column(unique = false, nullable = true)
	@XmlElement(name = "Attributes")
	public Attributes<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public Object get(String key) {
		return attributes == null ? null : attributes.get(key);
	}
	
	public void put(String key, Object value) {
		if (attributes == null) {
			attributes = new Attributes<String, Object>();
		}
		attributes.put(key, value);
	}
}
