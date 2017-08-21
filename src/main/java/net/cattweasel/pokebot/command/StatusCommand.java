package net.cattweasel.pokebot.command;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class StatusCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(StatusCommand.class);
	
	public StatusCommand() {
		super("status", "Zeigt den aktuellen Status des Bots");
	}
	
	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			QueryOptions qo = new QueryOptions();
			qo.addFilter(Filter.notnull("raidPokemon"));
			int raids = context.countObjects(Gym.class, qo);
			qo = new QueryOptions();
			qo.setLimit(1);
			qo.setOrder("created", "DESC");
			List<UserNotification> msgs = context.getObjects(UserNotification.class, qo);
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
			String lastMessage = msgs == null || msgs.isEmpty() ? "n/a" : sdf.format(msgs.get(0).getCreated()) + " Uhr";
			sendMessage(sender, chat, String.format("Benutzer insgesamt: %s (Davon aktiv: %s)"
					+ "\nArenen: %s (Davon Raids: %s)\nLetzte Mitteilung: %s",
					Util.separateNumber(context.countObjects(net.cattweasel.pokebot.object.User.class)),
					Util.separateNumber(context.countObjects(BotSession.class)),
					Util.separateNumber(context.countObjects(Gym.class)), Util.separateNumber(raids), lastMessage));
		} catch (GeneralException ex) {
			LOG.error("Error executing status command: " + ex.getMessage(), ex);
		} finally {
			if (context != null) {
				try {
					PokeFactory.releaseContext(context);
				} catch (GeneralException ex) {
					LOG.error("Error releasing context: " + ex.getMessage(), ex);
				}
			}
		}
	}
}
