package net.cattweasel.pokebot.api;

import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.commandbot.TelegramLongPollingCommandBot;

import net.cattweasel.pokebot.command.MenuCommand;
import net.cattweasel.pokebot.command.StartCommand;
import net.cattweasel.pokebot.command.StopCommand;

public class TelegramBot extends TelegramLongPollingCommandBot {

	private String botToken;
	
	public TelegramBot(String botUsername) {
		super(botUsername);
		register(new StartCommand());
		register(new MenuCommand());
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
		
		// TODO !!!
		
		System.out.println("\n** debug: location update!");
		System.out.println("** debug: chat: " + chat);
		System.out.println("** debug: user: " + user);
		System.out.println("** debug: location: " + location);
		
	}
}
