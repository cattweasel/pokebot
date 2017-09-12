package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.BotKeyboard;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public abstract class AbstractCommand extends BotCommand {

	private net.cattweasel.pokebot.object.User resolvedUser;
	
	private static final Logger LOG = Logger.getLogger(AbstractCommand.class);
	
	public AbstractCommand(String commandIdentifier, String description) {
		super(commandIdentifier, description);
	}
	
	@SuppressWarnings("deprecation")
	protected void sendMessage(AbsSender sender, Chat chat, String message) {
		SendMessage msg = new SendMessage();
		msg.setChatId(chat.getId());
		msg.setText(message);
		msg.setReplyMarkup(new BotKeyboard());
		try {
			sender.sendMessage(msg);
		} catch (TelegramApiException ex) {
			LOG.error(ex);
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void sendErrorMessage(AbsSender sender, Chat chat,
			net.cattweasel.pokebot.object.User user, Exception ex) {
		SendMessage msg = new SendMessage();
		msg.setChatId(chat.getId());
		msg.setText(Localizer.localize(user, "cmd_unknown_error"));
		msg.setReplyMarkup(new BotKeyboard());
		try {
			sender.sendMessage(msg);
		} catch (TelegramApiException e) {
			LOG.error(e);
		}
	}
	
	protected net.cattweasel.pokebot.object.User resolveUser(PokeContext context, User user) {
		if (resolvedUser == null) {
			try {
				resolvedUser = context.getObjectByName(net.cattweasel.pokebot.object.User.class, Util.otos(user.getId()));
				if (resolvedUser == null) {
					resolvedUser = new net.cattweasel.pokebot.object.User();
					resolvedUser.setFirstname(user.getFirstName());
					resolvedUser.setLanguageCode(user.getLanguageCode());
					resolvedUser.setLastname(user.getLastName());
					resolvedUser.setName(Util.otos(user.getId()));
					resolvedUser.setUsername(user.getUserName());
					context.saveObject(resolvedUser);
					Auditor auditor = new Auditor(context);
					auditor.log(Auditor.SYSTEM, AuditAction.CREATE_USER, resolvedUser.getName());
					context.commitTransaction();
				}
			} catch (GeneralException ex) {
				LOG.error("Error resolving User: " + ex.getMessage(), ex);
			}
		}
		return resolvedUser;
	}
}
