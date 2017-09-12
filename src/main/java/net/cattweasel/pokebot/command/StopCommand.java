package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.api.Terminator;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;

public class StopCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(StopCommand.class);
	
	public StopCommand() {
		super("stop", "Deaktiviert Meldungen von deinem Bot");
	}

	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		BotSession session = null;
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			String name = String.format("%s:%s", chat.getId(), user.getId());
			session = context.getObjectByName(BotSession.class, name);
			net.cattweasel.pokebot.object.User usr = resolveUser(context, user);
			if (session != null) {
				LOG.debug("Destroying BotSession: " + session);
				Auditor auditor = new Auditor(context);
				auditor.log(session.getUser().getName(), AuditAction.STOP_BOT_SESSION, session.getName());
				Terminator terminator = new Terminator(context);
				terminator.deleteObject(session);
				context.commitTransaction();
				sendMessage(sender, chat, Localizer.localize(usr, "cmd_stop_success_message"));
			} else {
				sendMessage(sender, chat, Localizer.localize(usr, "cmd_stop_failure_message"));
			}
		} catch (GeneralException ex) {
			LOG.error("Error executing stop command: " + ex.getMessage(), ex);
			sendErrorMessage(sender, chat, resolveUser(context, user), ex);
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
