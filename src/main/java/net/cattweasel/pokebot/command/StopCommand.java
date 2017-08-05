package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.tools.GeneralException;

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
			if (session != null) {
				LOG.debug("Destroying BotSession: " + session);
				context.removeObject(session);
				context.commitTransaction();
				sendMessage(sender, chat, String.format("Alles klar, %s, mach's gut, bis demnächst!",
						getDisplayableName(user)));
			} else {
				sendMessage(sender, chat, String.format("Nicht nötig, %s, ich arbeite doch gerade garnicht für dich!",
						getDisplayableName(user)));
			}
		} catch (GeneralException ex) {
			LOG.error("Error executing StopCommand: " + ex.getMessage(), ex);
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
