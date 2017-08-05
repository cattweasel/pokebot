package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "db_bot_session")
@XmlRootElement(name = "BotSession")
public class BotSession extends PokeObject {

	private static final long serialVersionUID = 8299634003354015640L;
	
	private Long chatId;
	private Integer userId;
	
	@Column(unique = true, nullable = false)
	@XmlAttribute
	public Long getChatId() {
		return chatId;
	}
	
	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}

	@Column(unique = true, nullable = false)
	@XmlAttribute
	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}