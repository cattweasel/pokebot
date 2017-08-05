package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.tools.GeneralException;

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
			if (session == null) {
				session = new BotSession();
				session.setName(name);
				session.setChatId(chat.getId());
				session.setUser(resolveUser(context, user));
				LOG.debug("Creating new BotSession: " + session);
				sendMessage(sender, chat, String.format("Hey, %s! Schön dich zu sehen!",
						getDisplayableName(user)));
			} else {
				LOG.debug("Re-allocating BotSession: " + session);
				sendMessage(sender, chat, String.format("Hey, %s! Ich bin noch immer für dich am arbeiten!",
						getDisplayableName(user)));
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
}
