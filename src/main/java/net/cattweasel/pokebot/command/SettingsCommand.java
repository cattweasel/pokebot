package net.cattweasel.pokebot.command;

import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

public class SettingsCommand extends AbstractCommand {
	
	public SettingsCommand() {
		super("settings", "Liefert die URL für deine Konfiguration");
	}

	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		sendMessage(sender, chat, String.format("https://nyapgbot.net/settings.jsf?id=%s", user.getId()));
	}
}
