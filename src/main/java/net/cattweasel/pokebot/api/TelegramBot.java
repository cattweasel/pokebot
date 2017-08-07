package net.cattweasel.pokebot.api;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.command.SettingsCommand;
import net.cattweasel.pokebot.command.StartCommand;
import net.cattweasel.pokebot.command.StopCommand;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.tools.GeneralException;

public class TelegramBot extends TelegramLongPollingCommandBot {

	private String botToken;
	
	private static final Logger LOG = Logger.getLogger(TelegramBot.class);
	
	public TelegramBot(String botUsername) {
		super(botUsername);
		register(new StartCommand());
		register(new SettingsCommand());
		register(new StopCommand());
	}
	
	@Override
	public String getBotToken() {
		return botToken;
	}
	
	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	@Override
	public void processNonCommandUpdate(Update update) {
		if (update.getMessage() != null && update.getMessage().getLocation() != null) {
			handleLocationUpdate(update.getMessage().getChat(), update.getMessage().getFrom(),
					update.getMessage().getLocation());
		}
	}
	
	private void handleLocationUpdate(Chat chat, User user, Location location) {
		BotSession session = null;
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			session = context.getObjectByName(BotSession.class,
					String.format("%s:%s", chat.getId(), user.getId()));
			if (session != null) {
				LOG.debug("Updating Location: " + user + " -> " + location);
				session.put("latitude", location.getLatitude());
				session.put("longitude", location.getLongitude());
				context.saveObject(session);
				context.commitTransaction();
				confirmLocation(chat, user, location);
			} else {
				informNotWorking(chat, user, location);
			}
		} catch (GeneralException ex) {
			LOG.error("Error handling location update: " + ex.getMessage(), ex);
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
	
	@SuppressWarnings("deprecation")
	private void confirmLocation(Chat chat, User user, Location location) {
		SendMessage message = new SendMessage();
		message.setChatId(chat.getId());
		message.setText(String.format("Alles klar, deine neue Position ist nun: %s / %s",
				location.getLatitude(), location.getLongitude()));
		try {
			sendMessage(message);
		} catch (TelegramApiException ex) {
			LOG.error("Error confirming location change: " + ex.getMessage(), ex);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void informNotWorking(Chat chat, User user, Location location) {
		SendMessage message = new SendMessage();
		message.setChatId(chat.getId());
		message.setText("Ich bin gerade nicht am arbeiten. Du musst mich zuerst aktivieren!");
		try {
			sendMessage(message);
		} catch (TelegramApiException ex) {
			LOG.error("Error informing not working: " + ex.getMessage(), ex);
		}
	}
}
