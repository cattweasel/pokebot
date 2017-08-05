package net.cattweasel.pokebot.command;

import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

public class MenuCommand extends AbstractCommand {
	
	public MenuCommand() {
		super("menu", "Öffnet dein persönliches Bot-Menü");
	}

	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		
		// TODO !!!
		
	}
}
