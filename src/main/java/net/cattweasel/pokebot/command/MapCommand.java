package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class MapCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(MapCommand.class);
	
	public MapCommand() {
		super("map", "Liefert die URL zu deiner Map");
	}
	
	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			net.cattweasel.pokebot.object.User usr = resolveUser(context, user);
			String link = String.format("https://www.nyapgbot.net/map.jsf?id=%s", usr.getId());
			sendMessage(sender, chat, String.format("%s: %s", Localizer.localize(usr,
					"cmd_map_success_message"), link));
			Auditor auditor = new Auditor(context);
			auditor.log(Util.otos(user.getId()), AuditAction.GET_MAP_LINK, link);
			context.commitTransaction();
		} catch (GeneralException ex) {
			LOG.error("Error executing map command: " + ex.getMessage(), ex);
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
