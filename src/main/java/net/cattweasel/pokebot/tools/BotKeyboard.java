package net.cattweasel.pokebot.tools;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

public class BotKeyboard extends ReplyKeyboardMarkup {
	
	private static final long serialVersionUID = -2469636094973339413L;
	
	public BotKeyboard() {
		List<KeyboardRow> rows = new ArrayList<KeyboardRow>();
		KeyboardRow row1 = new KeyboardRow();
		row1.add(new KeyboardButton("/start"));
		row1.add(new KeyboardButton("/stop"));
		row1.add(new KeyboardButton("/reset"));
		row1.add(new KeyboardButton("/status"));
		rows.add(row1);
		KeyboardRow row2 = new KeyboardRow();
		row2.add(new KeyboardButton("/help"));
		row2.add(new KeyboardButton("/settings"));
		row2.add(new KeyboardButton("/location").setRequestLocation(true));
		row2.add(new KeyboardButton("/map"));
		row2.add(new KeyboardButton("/history"));
		rows.add(row2);
		setKeyboard(rows);
	}
}
