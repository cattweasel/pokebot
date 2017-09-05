package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class HistoryCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(HistoryCommand.class);
	
	public HistoryCommand() {
		super("history", "Liefert die URL zu deinem Verlauf");
	}
	
	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			net.cattweasel.pokebot.object.User usr = resolveUser(context, user);
			String link = String.format("https://www.nyapgbot.net/history.jsf?id=%s", usr.getId());
			sendMessage(sender, chat, String.format("%s: %s", Localizer.localize(usr,
					"cmd_history_success_message"), link));
			Auditor auditor = new Auditor(context);
			auditor.log(Util.otos(user.getId()), AuditAction.GET_HISTORY_LINK, link);
			context.commitTransaction();
		} catch (GeneralException ex) {
			LOG.error("Error executing history command: " + ex.getMessage(), ex);
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
