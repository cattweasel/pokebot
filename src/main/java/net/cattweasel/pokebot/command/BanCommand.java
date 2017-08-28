package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.Capability;
import net.cattweasel.pokebot.tools.CapabilityManager;
import net.cattweasel.pokebot.tools.GeneralException;

public class BanCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(BanCommand.class);
	
	public BanCommand() {
		super("/ban", "Sperrt einen Benutzer dauerhaft");
	}

	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			String userId = args.length == 0 ? null : args[0];
			if (userId == null) {
				sendMessage(sender, chat, "User-ID angeben!"); // TODO
			} else {
				net.cattweasel.pokebot.object.User usr = context.getObjectByName(
						net.cattweasel.pokebot.object.User.class, userId);
				if (usr == null) {
					sendMessage(sender, chat, "User-ID nicht gefunden!"); // TODO
				} else {
					Capability cap = context.getObjectByName(Capability.class, Capability.SYSTEM_ADMINISTRATOR);
					if (!CapabilityManager.hasCapability(resolveUser(context, user), cap)) {
						sendMessage(sender, chat, "Du darfst das nicht ;-P"); // TODO
					} else {
						usr.setBanned(true);
						context.saveObject(usr);
						context.commitTransaction();
						sendMessage(sender, chat, "User wurde gebannt!"); // TODO
					}
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error executing ban command: " + ex.getMessage(), ex);
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
