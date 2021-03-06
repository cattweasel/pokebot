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

	public static final String BAN_USER = "BanUser";
	public static final String CREATE_PROFILE = "CreateProfile";
	public static final String CREATE_USER = "CreateUser";
	public static final String DELETE_PROFILE = "DeleteProfile";
	public static final String GET_BOT_STATUS = "GetBotStatus";
	public static final String GET_HELP_LINK = "GetHelpLink";
	public static final String GET_HISTORY_LINK = "GetHistoryLink";
	public static final String GET_MAP_LINK = "GetMapLink";
	public static final String GET_SETTINGS_LINK = "GetSettingsLink";
	public static final String LOAD_PROFILE = "LoadProfile";
	public static final String RESET_NOTIFICATIONS = "ResetNotifications";
	public static final String SAVE_PROFILE = "SaveProfile";
	public static final String SEND_BROADCAST = "SendBroadcast";
	public static final String SEND_RAID_NOTIFICATION = "SendRaidNotification";
	public static final String SEND_SPAWN_NOTIFICATION = "SendSpawnNotification";
	public static final String SEND_UPDATE_NOTIFICATION = "SendUpdateNotification";
	public static final String START_BOT_SESSION = "StartBotSession";
	public static final String STOP_BOT_SESSION = "StopBotSession";
	public static final String UNBAN_USER = "UnbanUser";
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
