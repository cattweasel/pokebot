package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "db_audit_action")
@XmlRootElement(name = "AuditAction")
public class AuditAction extends PokeObject {

	public static final String CREATE_USER = "CreateUser";
	public static final String GET_BOT_STATUS = "GetBotStatus";
	public static final String GET_SETTINGS_LINK = "GetSettingsLink";
	public static final String RESET_NOTIFICATIONS = "ResetNotifications";
	public static final String SEND_BROADCAST = "SendBroadcast";
	public static final String SEND_RAID_NOTIFICATION = "SendRaidNotification";
	public static final String SEND_SPAWN_NOTIFICATION = "SendSpawnNotification";
	public static final String START_BOT_SESSION = "StartBotSession";
	public static final String STOP_BOT_SESSION = "StopBotSession";
	public static final String UPDATE_LOCATION = "UpdateLocation";
	public static final String UPDATE_SETTINGS = "UpdateSettings";
	
	private static final long serialVersionUID = 8876481600343758120L;
	
	private Boolean enabled;
	
	@Column(unique = false, nullable = false)
	@XmlAttribute
	public Boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
}
