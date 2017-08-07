package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.tools.GeneralException;

public class SettingsCommand extends AbstractCommand {
	
	private static final Logger LOG = Logger.getLogger(SettingsCommand.class);
	
	public SettingsCommand() {
		super("settings", "Liefert die URL für deine Konfiguration");
	}

	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			net.cattweasel.pokebot.object.User usr = resolveUser(context, user);
			sendMessage(sender, chat, String.format("Deine persönlichen Einstellungen:"
					+ " https://www.nyapgbot.net/settings.jsf?id=%s", usr.getId()));
		} catch (GeneralException ex) {
			LOG.error("Error executing SettingsCommand: " + ex.getMessage(), ex);
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
