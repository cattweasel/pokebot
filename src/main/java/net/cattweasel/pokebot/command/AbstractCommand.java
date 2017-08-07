package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public abstract class AbstractCommand extends BotCommand {

	private static final Logger LOG = Logger.getLogger(AbstractCommand.class);
	
	public AbstractCommand(String commandIdentifier, String description) {
		super(commandIdentifier, description);
	}
	
	@SuppressWarnings("deprecation")
	protected void sendMessage(AbsSender sender, Chat chat, String message) {
		SendMessage msg = new SendMessage();
		msg.setChatId(chat.getId());
		msg.setText(message);
		try {
			sender.sendMessage(msg);
		} catch (TelegramApiException ex) {
			LOG.error(ex);
		}
	}
	
	protected net.cattweasel.pokebot.object.User resolveUser(PokeContext context, User user) {
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
