package net.cattweasel.pokebot.telegram;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.commandbot.TelegramLongPollingCommandBot;

import net.cattweasel.pokebot.telegram.command.MenuCommand;
import net.cattweasel.pokebot.telegram.command.StartCommand;
import net.cattweasel.pokebot.telegram.command.StopCommand;

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
		
		System.out.println("\n** debug: non-command update: " + update); // TODO
		
	}
}
