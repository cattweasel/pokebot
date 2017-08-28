package net.cattweasel.pokebot.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.api.Terminator;
import net.cattweasel.pokebot.command.BanCommand;
import net.cattweasel.pokebot.command.BroadcastCommand;
import net.cattweasel.pokebot.command.HelpCommand;
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
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.RaidRegistration;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class TelegramBot extends TelegramLongPollingCommandBot {

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
		} else if (update.getCallbackQuery() != null) {
			handleCallbackQuery(update.getCallbackQuery());
		} else {
			LOG.warn("Unknown bot command: " + update);
		}
	}
	
	private void handleCallbackQuery(CallbackQuery query) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			Gym gym = context.getObjectByName(Gym.class, query.getData());
			BotSession session = context.getObjectByName(BotSession.class, String.format("%s:%s",
					query.getMessage().getChat().getId(), query.getFrom().getId()));
			if (gym != null && session != null) {
				handleRaidJoin(context, gym, session, query);
			} else {
				gym = context.getObjectByName(Gym.class, query.getData().split(":")[0]);
				if (gym != null) {
					handleRaidUpdate(context, gym, session, query);
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error handling callback query: " + ex.getMessage(), ex);
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
	private void handleRaidJoin(PokeContext context, Gym gym, BotSession session,
			CallbackQuery query) throws GeneralException {
		EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
		emrm.setChatId(session.getChatId());
		emrm.setMessageId(query.getMessage().getMessageId());
		emrm.setReplyMarkup(createReplyMarkup(context, session, gym));
		try {
			editMessageReplyMarkup(emrm);
		} catch (TelegramApiException ex) {
			LOG.error(ex);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void handleRaidUpdate(PokeContext context, Gym gym,
			BotSession session, CallbackQuery query) throws GeneralException {
		String[] parts = query.getData().split(":");
		if (parts.length < 3) {
			RaidRegistration reg = new RaidRegistration();
			reg.setName(String.format("%s:%s:%s", gym.getName(), session.getUser().getName(),
					new Date(Util.atol(parts[1])).getTime()));
			context.saveObject(reg);
		} else {
			RaidRegistration reg = context.getObjectByName(RaidRegistration.class, query.getData());
			Terminator terminator = new Terminator(context);
			terminator.deleteObject(reg);
		}
		context.commitTransaction();
		EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
		emrm.setChatId(session.getChatId());
		emrm.setMessageId(query.getMessage().getMessageId());
		emrm.setReplyMarkup(createReplyMarkup(context, session, gym));
		try {
			editMessageReplyMarkup(emrm);
		} catch (TelegramApiException ex) {
			LOG.error(ex);
		}
	}
	
	private InlineKeyboardMarkup createReplyMarkup(PokeContext context, BotSession session,
			Gym gym) throws GeneralException {
		InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();
		List<InlineKeyboardButton> buttons = new ArrayList<InlineKeyboardButton>();
		String name = String.format("%s:%s:", gym.getName(), session.getUser().getName());
		RaidRegistration reg = context.getUniqueObject(RaidRegistration.class,
				Filter.like(ExtendedAttributes.POKE_OBJECT_NAME, name, Filter.MatchMode.START));
		if (reg == null) {
			for (Date date : calculateRaidDates(gym.getRaidEnd())) {
				InlineKeyboardButton ikb = new InlineKeyboardButton();
				ikb.setCallbackData(String.format("%s:%s", gym.getName(), date.getTime()));
				ikb.setText(Localizer.localize(session.getUser(), date, false));
				buttons.add(ikb);
			}
		} else {
			InlineKeyboardButton ikb = new InlineKeyboardButton();
			ikb.setCallbackData(reg.getName());
			ikb.setText(String.format("%s: %s", Localizer.localize(session.getUser(), "registration"),
					Localizer.localize(session.getUser(), new Date(Util.atol(reg.getName().split(":")[2])))));
			buttons.add(ikb);
		}
		ikm.setKeyboard(Arrays.asList(buttons));
		return ikm;
	}
	
	private List<Date> calculateRaidDates(Date origin) {
		List<Date> dates = new ArrayList<Date>();
		Date date = new Date(origin.getTime() - 1200000L);
		int counter = 1;
		while (counter <= 4 && date.getTime() >= new Date().getTime()) {
			dates.add(new Date(date.getTime()));
			date = new Date(date.getTime() - 1200000L);
			counter++;
		}
		return dates;
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
