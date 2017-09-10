package net.cattweasel.pokebot.command;

import java.util.List;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class StatusCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(StatusCommand.class);
	
	public StatusCommand() {
		super("status", "Zeigt den aktuellen Status des Bots");
	}
	
	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			QueryOptions qo = new QueryOptions();
			qo.addFilter(Filter.notnull(ExtendedAttributes.GYM_RAID_POKEMON));
			int raids = context.countObjects(Gym.class, qo);
			qo = new QueryOptions();
			qo.setLimit(1);
			qo.setOrder(ExtendedAttributes.POKE_OBJECT_CREATED, "DESC");
			List<UserNotification> msgs = context.getObjects(UserNotification.class, qo);
			net.cattweasel.pokebot.object.User usr = resolveUser(context, user);
			String sessionState = context.getUniqueObject(BotSession.class, Filter.eq(ExtendedAttributes.BOT_SESSION_USER, usr)) == null
					? Localizer.localize(usr, "inactive") : Localizer.localize(usr, "active");
			String lastMessage = msgs == null || msgs.isEmpty() ? "n/a" : Localizer.localize(usr, msgs.get(0).getCreated(), true);
			sendMessage(sender, chat, String.format("%s: %s (%s: %s)%n%s: %s (%s: %s)%n%s: %s%n%s: %s", Localizer.localize(usr, "total_users"),
					Util.separateNumber(context.countObjects(net.cattweasel.pokebot.object.User.class)),
					Localizer.localize(usr, "thereof_active"), Util.separateNumber(context.countObjects(BotSession.class)),
					Localizer.localize(usr, "gyms"), Util.separateNumber(context.countObjects(Gym.class)),
					Localizer.localize(usr, "thereof_raids"), Util.separateNumber(raids),
					Localizer.localize(usr, "last_message"), lastMessage,
					Localizer.localize(usr, "session_state"), sessionState));
			Auditor auditor = new Auditor(context);
			auditor.log(Util.otos(user.getId()), AuditAction.GET_BOT_STATUS, Util.otos(chat.getId()));
			context.commitTransaction();
			// TODO: Benutzer wird über Fehler nicht informiert, es wird lediglich serverseitig geloggt
		} catch (GeneralException ex) {
			LOG.error("Error executing status command: " + ex.getMessage(), ex);
		} finally {
			if (context != null) {
				try {
					PokeFactory.releaseContext(context);
				} catch (GeneralException ex) {
					LOG.error("Error releasing context: " + ex.getMessage(), ex);
				}
			}
		}
	}
}
