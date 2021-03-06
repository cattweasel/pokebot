package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.Configuration;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class HelpCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(HelpCommand.class);
	
	public HelpCommand() {
		super("help", "Liefert den Link zur Hilfe");
	}
	
	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			net.cattweasel.pokebot.object.User usr = resolveUser(context, user);
			Configuration config = context.getObjectByName(Configuration.class, Configuration.SYSTEM_CONFIGURATION);
			String link = String.format("%s/help.jsf?id=%s", Util.otos(config.get("baseUrl")), createOnetimeLink(context, usr).getName());
			sendMessage(sender, chat, String.format("%s: %s", Localizer.localize(usr, "cmd_help_success_message"), link));
			Auditor auditor = new Auditor(context);
			auditor.log(Util.otos(user.getId()), AuditAction.GET_HELP_LINK, link);
			context.commitTransaction();
		} catch (GeneralException ex) {
			LOG.error("Error executing help command: " + ex.getMessage(), ex);
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
