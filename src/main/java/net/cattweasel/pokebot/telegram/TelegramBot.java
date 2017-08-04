package net.cattweasel.pokebot.telegram;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class TelegramBot extends TelegramLongPollingBot {

	private String botUsername;
	private String botToken;
	
	@Override
	public String getBotUsername() {
		return botUsername;
	}
	
	public void setBotUsername(String botUsername) {
		this.botUsername = botUsername;
	}
	
	@Override
	public String getBotToken() {
		return botToken;
	}
	
	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	@Override
	public void onUpdateReceived(Update update) {
		
		System.out.println("** debug: update: " + update); // TODO
		
	}
}
