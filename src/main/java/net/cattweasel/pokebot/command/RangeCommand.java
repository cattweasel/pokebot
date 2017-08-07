package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class RangeCommand extends AbstractCommand {

	private static final String ARG_RANGE = "range";
	
	private static final Logger LOG = Logger.getLogger(RangeCommand.class);
	
	public RangeCommand() {
		super("range", "Definiert die Reichweite für deinen Bot");
	}

	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		if (args == null || args.length == 0) {
			sendMessage(sender, chat, String.format("Du musst einen gültigen Wert angeben (100 - 20000)."
					+ " Alle Angaben in Meter!"));
		} else {
			int range = Util.otoi(args[0]);
			if (range < 100 || range > 20000) {
				sendMessage(sender, chat, String.format("Du musst einen gültigen Wert angeben (100 - 20000)."
						+ " Alle Angaben in Meter!"));
			} else {
				handleUpdate(sender, chat, user, range);
			}
		}
	}
	
	private void handleUpdate(AbsSender sender, Chat chat, User user, Integer range) {
		BotSession session = null;
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			String name = String.format("%s:%s", chat.getId(), user.getId());
			session = context.getObjectByName(BotSession.class, name);
			if (session != null) {
				session.put(ARG_RANGE, range);
				context.saveObject(session);
				context.commitTransaction();
				sendMessage(sender, chat, String.format(
						"Alles klar, dein Radius ist nun auf %sm eingestellt!", range));
			}
		} catch (GeneralException ex) {
			LOG.error("Error executing RangeCommand: " + ex.getMessage(), ex);
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
