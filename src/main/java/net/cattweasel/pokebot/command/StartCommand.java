package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;

public class StartCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(StartCommand.class);
	
	public StartCommand() {
		super("start", "Aktiviert Meldungen von deinem Bot");
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
			if (session == null) {
				session = new BotSession();
				session.setName(name);
				session.setChatId(chat.getId());
				session.setUser(resolveUser(context, user));
				session.put("range", 3000);
				LOG.debug("Creating new BotSession: " + session);
				sendMessage(sender, chat, Localizer.localize(usr, "cmd_start_success_message"));
				Auditor auditor = new Auditor(context);
				auditor.log(session.getUser().getName(), AuditAction.START_BOT_SESSION, session.getName());
			} else {
				LOG.debug("Re-allocating BotSession: " + session);
				sendMessage(sender, chat, Localizer.localize(usr, "cmd_start_failure_message"));
			}
			context.saveObject(session);
			context.commitTransaction();
		} catch (GeneralException ex) {
			LOG.error("Error executing start command: " + ex.getMessage(), ex);
		} finally {
			if (context != null) {
				try {
					PokeFactory.releaseContext(context);
				} catch (GeneralException ex) {
					LOG.error("Error releasing PokeContext: " + ex.getMessage(), ex);
				}
			}
		}
	}
}
