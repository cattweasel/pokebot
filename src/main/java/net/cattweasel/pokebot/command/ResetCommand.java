package net.cattweasel.pokebot.command;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.api.Terminator;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class ResetCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(ResetCommand.class);
	
	public ResetCommand() {
		super("reset", "Erneuert sämtliche Meldungen");
	}

	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			Terminator terminator = new Terminator(context);
			QueryOptions qo = new QueryOptions();
			qo.addFilter(Filter.like(ExtendedAttributes.POKE_OBJECT_NAME,
					resolveUser(context, user).getName() + ":", Filter.MatchMode.START));
			Iterator<String> it = context.search(UserNotification.class, qo);
			if (it != null) {
				while (it.hasNext()) {
					UserNotification n = context.getObjectById(UserNotification.class, it.next());
					if (n != null) {
						terminator.deleteObject(n);
					}
				}
				net.cattweasel.pokebot.object.User usr = resolveUser(context, user);
				Auditor auditor = new Auditor(context);
				auditor.log(Util.otos(user.getId()), AuditAction.RESET_NOTIFICATIONS, Util.otos(user.getId()));
				context.commitTransaction();
				sendMessage(sender, chat, Localizer.localize(usr, "cmd_reset_success_message"));
			}
		} catch (GeneralException ex) {
			LOG.error("Error executing reset command: " + ex.getMessage(), ex);
			sendErrorMessage(sender, chat, resolveUser(context, user), ex);
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
