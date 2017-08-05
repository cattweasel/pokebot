package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class StartCommand extends BotCommand {

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
			if (session == null) {
				session = new BotSession();
				session.setName(name);
				session.setChatId(chat.getId());
				session.setUser(resolveUser(context, user));
				LOG.debug("Creating new BotSession: " + session);
			} else {
				LOG.debug("Re-allocating BotSession: " + session);
			}
			context.saveObject(session);
			context.commitTransaction();
		} catch (GeneralException ex) {
			LOG.error("Error executing StartCommand: " + ex.getMessage(), ex);
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
	
	private net.cattweasel.pokebot.object.User resolveUser(PokeContext context, User user) {
		net.cattweasel.pokebot.object.User result = null;
		try {
			result = context.getObjectByName(net.cattweasel.pokebot.object.User.class, Util.otos(user.getId()));
			if (result == null) {
				result = new net.cattweasel.pokebot.object.User();
				result.setFirstname(user.getFirstName());
				result.setLanguageCode(user.getLanguageCode());
				result.setLastname(user.getLastName());
				result.setName(Util.otos(user.getId()));
				result.setUsername(user.getUserName());
				context.saveObject(result);
				context.commitTransaction();
			}
		} catch (GeneralException ex) {
			LOG.error("Error resolving User: " + ex.getMessage(), ex);
		}
		return result;
	}
}
