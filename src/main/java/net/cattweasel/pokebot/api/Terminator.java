package net.cattweasel.pokebot.api;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.server.Environment;
import net.cattweasel.pokebot.server.TelegramBot;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class Terminator {

	private final PokeContext context;
	
	private static final Logger LOG = Logger.getLogger(Terminator.class);
	
	public Terminator(PokeContext context) {
		this.context = context;
	}
	
	public <T extends PokeObject> void deleteObject(T object) throws GeneralException {
		if (object != null) {
			handleObjectRelations(object);
			context.removeObject(object);
		}
	}
	
	private <T extends PokeObject> void handleObjectRelations(T object) throws GeneralException {
		if (object instanceof BotSession) {
			handleBotSessionRelations((BotSession) object);
		} else if (object instanceof UserNotification) {
			handleUserNotificationRelations((UserNotification) object);
		}
	}

	private void handleBotSessionRelations(BotSession session) throws GeneralException {
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.like(ExtendedAttributes.POKE_OBJECT_NAME,
				String.format("%s:", session.getUser().getName()), Filter.MatchMode.START));
		Iterator<String> it = context.search(UserNotification.class, qo);
		if (it != null) {
			while (it.hasNext()) {
				UserNotification notification = context.getObjectById(UserNotification.class, it.next());
				if (notification != null) {
					deleteObject(notification);
				}
			}
		}
	}

	private void handleUserNotificationRelations(UserNotification notification) throws GeneralException {
		if (Util.isNotNullOrEmpty(notification.getMessageId())) {
			User user = context.getObjectByName(User.class, notification.getName().split(":")[0]);
			if (user != null) {
				if ((user.getSettings() == null || user.getSettings().get(
						ExtendedAttributes.USER_SETTINGS_DELETE_EXPIRED) == null)
						|| (user.getSettings() != null && Util.otob(user.getSettings().get(
						ExtendedAttributes.USER_SETTINGS_DELETE_EXPIRED)))) {
					deleteUserMessages(notification.getMessageId());
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void deleteUserMessages(String messageId) {
		TelegramBot bot = Environment.getEnvironment().getTelegramBot();
		String[] parts = messageId.split("-");
		for (int i=0; i<parts.length; i++) {
			DeleteMessage m = new DeleteMessage();
			m.setChatId(parts[i].split(":")[0]);
			m.setMessageId(Util.otoi(parts[i].split(":")[1]));
			try {
				bot.deleteMessage(m);
			} catch (TelegramApiException | IndexOutOfBoundsException ex) {
				LOG.error("Error deleting message: " + ex.getMessage(), ex);
			}
		}
	}
}
