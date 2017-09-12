package net.cattweasel.pokebot.server;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.command.BanCommand;
import net.cattweasel.pokebot.command.BroadcastCommand;
import net.cattweasel.pokebot.command.HelpCommand;
import net.cattweasel.pokebot.command.HistoryCommand;
import net.cattweasel.pokebot.command.MapCommand;
import net.cattweasel.pokebot.command.ResetCommand;
import net.cattweasel.pokebot.command.SettingsCommand;
import net.cattweasel.pokebot.command.StartCommand;
import net.cattweasel.pokebot.command.StatusCommand;
import net.cattweasel.pokebot.command.StopCommand;
import net.cattweasel.pokebot.command.UnbanCommand;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class TelegramBot extends TelegramLongPollingCommandBot {

	// TODO: Flood-Protection for commands and non-commands
	
	private String botToken;
	
	private static final Logger LOG = Logger.getLogger(TelegramBot.class);
	
	public TelegramBot(String botUsername) {
		super(botUsername);
		register(new BanCommand());
		register(new BroadcastCommand());
		register(new HelpCommand());
		register(new ResetCommand());
		register(new SettingsCommand());
		register(new StartCommand());
		register(new StatusCommand());
		register(new StopCommand());
		register(new UnbanCommand());
		register(new MapCommand());
		register(new HistoryCommand());
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
		} else {
			LOG.warn("Unknown bot command: " + update);
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
				session.put(ExtendedAttributes.BOT_SESSION_LATITUDE, location.getLatitude());
				session.put(ExtendedAttributes.BOT_SESSION_LONGITUDE, location.getLongitude());
				context.saveObject(session);
				Auditor auditor = new Auditor(context);
				auditor.log(session.getUser().getName(), AuditAction.UPDATE_LOCATION,
						String.format("%s:%s", location.getLatitude(), location.getLongitude()));
				confirmLocation(context, chat, user, location);
			} else {
				informNotWorking(context, chat, user, location);
			}
			context.commitTransaction();
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
	private void confirmLocation(PokeContext context, Chat chat, User user, Location location) throws GeneralException {
		net.cattweasel.pokebot.object.User usr = context.getUniqueObject(net.cattweasel.pokebot.object.User.class,
				Filter.eq(ExtendedAttributes.POKE_OBJECT_NAME, Util.otos(user.getId())));
		SendMessage message = new SendMessage();
		message.setChatId(chat.getId());
		message.setText(String.format("%s: %s / %s", Localizer.localize(usr, "bot_location_success_message"),
				location.getLatitude(), location.getLongitude()));
		try {
			sendMessage(message);
		} catch (TelegramApiException ex) {
			LOG.error("Error confirming location change: " + ex.getMessage(), ex);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void informNotWorking(PokeContext context, Chat chat, User user, Location location) throws GeneralException {
		net.cattweasel.pokebot.object.User usr = context.getUniqueObject(net.cattweasel.pokebot.object.User.class,
				Filter.eq(ExtendedAttributes.POKE_OBJECT_NAME, Util.otos(user.getId())));
		SendMessage message = new SendMessage();
		message.setChatId(chat.getId());
		message.setText(Localizer.localize(usr, "bot_location_failure_message"));
		try {
			sendMessage(message);
		} catch (TelegramApiException ex) {
			LOG.error("Error informing not working: " + ex.getMessage(), ex);
		}
	}
}
