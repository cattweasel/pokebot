package net.cattweasel.pokebot.telegram.command;

import java.util.Arrays;

import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;

public class StopCommand extends BotCommand {

	public StopCommand() {
		super("stop", "Deaktiviert Meldungen von deinem Bot");
	}

	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		
		// TODO Auto-generated method stub
		
		System.out.println("\n** debug: executing stop command!");
		System.out.println("** debug: sender: " + sender);
		System.out.println("** debug: user: " + user);
		System.out.println("** debug: chat: " + chat);
		System.out.println("** debug: args: " + args == null ? null : Arrays.asList(args));
		
	}
}
